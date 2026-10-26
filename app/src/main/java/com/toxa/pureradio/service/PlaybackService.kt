package com.toxa.pureradio.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.toxa.pureradio.MainActivity

/**
 * Hosts the [ExoPlayer] and [MediaSession] as a proper foreground service so that
 * playback survives the app leaving the foreground (screen off, Home button, the
 * Picture-in-Picture window being swiped away, etc.) and so the system shows a
 * standard media notification with lock-screen / notification-shade controls.
 *
 * The app's own UI (MainViewModel) binds to this service directly (see [LocalBinder])
 * to get a raw [ExoPlayer] reference — everything in the ViewModel that used to call
 * into a self-owned ExoPlayer keeps working unchanged, it just talks to the player
 * living here instead.
 *
 * The session is registered with [addSession] as soon as it is built. Media3 only manages
 * the media notification and the foreground state for sessions that have been added; it
 * does that by itself when a MediaController connects through the standard service
 * interface, but our UI binds with a private action ([ACTION_LOCAL_BIND]) and never
 * connects a controller, so without the explicit call the session was never added — no
 * media notification / lock-screen controls were shown, and the service had to be forced
 * into the foreground with a permanent "Connecting…" placeholder. With the session added,
 * Media3 starts the service in the foreground (with its standard media notification) when
 * playback starts and drops it out of the foreground when playback pauses or stops.
 */
@UnstableApi
class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    inner class LocalBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    private val localBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("pure_radio_prefs", Context.MODE_PRIVATE)
        buildPlayer(
            extraBuffering = prefs.getBoolean("extra_buffering", false),
            audioPassthrough = prefs.getBoolean("audio_passthrough", false)
        )
    }

    /** Current player, or null if the service hasn't finished creating one yet. */
    fun getPlayer(): ExoPlayer? = player

    /**
     * (Re)builds the ExoPlayer with the given buffering/passthrough settings. Used both
     * for the initial player and whenever the user changes those settings at runtime —
     * ExoPlayer's buffer sizes and renderer configuration can only be set at construction
     * time, so changing them means building a new player instance.
     *
     * The current media item, position, and play state are carried over, and the same
     * [MediaSession] is kept (via [MediaSession.setPlayer]) rather than recreated, so the
     * system notification doesn't flicker and any connected controllers stay attached.
     */
    fun buildPlayer(extraBuffering: Boolean, audioPassthrough: Boolean): ExoPlayer {
        val previousPlayer = player
        val previousItem = previousPlayer?.currentMediaItem
        val previousPosition = previousPlayer?.currentPosition ?: 0L
        val wasPlaying = previousPlayer?.isPlaying == true

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory)

        val loadControl = if (extraBuffering) {
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    30000, // Min buffer 30s
                    60000, // Max buffer 60s
                    8000,  // Buffer for playback 8s
                    12000  // Buffer for playback after rebuffer 12s
                ).build()
        } else {
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15000, // Min buffer 15s
                    50000, // Max buffer 50s
                    2500,  // Buffer for playback 2.5s
                    5000   // Buffer for playback after rebuffer 5s
                ).build()
        }

        val builder = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)

        // "High-Resolution Output" setting (stored under the old "audio_passthrough" key).
        // Float output makes the platform decoders hand over 32-bit float PCM, so 24-bit
        // sources such as hi-res FLAC aren't truncated to 16-bit. It is NOT bit-perfect:
        // Android still mixes/resamples to the device rate. (The previous version also set
        // EXTENSION_RENDERER_MODE_PREFER, which did nothing because no Media3 decoder
        // extensions are bundled with the app.)
        if (audioPassthrough) {
            val renderersFactory = DefaultRenderersFactory(this)
                .setEnableAudioFloatOutput(true)
            builder.setRenderersFactory(renderersFactory)
        }

        builder.setAudioAttributes(audioAttributes, true)
        builder.setHandleAudioBecomingNoisy(true)
        builder.setSkipSilenceEnabled(false)

        val newPlayer = builder.build().apply {
            // Force 1.0 speed and pitch to ensure no resampling for time-stretching
            playbackParameters = PlaybackParameters.DEFAULT
            // "Best Stream Quality": adaptive (HLS) stations often offer the same programme at
            // several bitrates (e.g. 48 / 96 / 320 kbps) and ExoPlayer would otherwise start
            // low and climb slowly. Radio bitrates are small, so always take the top one.
            val bestStream = getSharedPreferences("pure_radio_prefs", Context.MODE_PRIVATE)
                .getBoolean(PREF_BEST_STREAM, true)
            trackSelectionParameters = trackSelectionParameters.buildUpon()
                .setForceHighestSupportedBitrate(bestStream)
                .build()
        }

        if (previousItem != null) {
            newPlayer.setMediaItem(previousItem)
            newPlayer.seekTo(previousPosition)
            newPlayer.prepare()
            if (wasPlaying) newPlayer.play()
        }

        val session = mediaSession
        if (session != null) {
            session.player = newPlayer
        } else {
            val newSession = MediaSession.Builder(this, newPlayer)
                .setSessionActivity(openAppIntent())
                .build()
            mediaSession = newSession
            // See the class comment: this is what makes Media3 show the media notification
            // and manage the foreground state for our session.
            addSession(newSession)
        }

        previousPlayer?.release()
        player = newPlayer
        return newPlayer
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onBind(intent: Intent?): IBinder? {
        // Our own app binds locally (see MainViewModel) to get a direct ExoPlayer reference.
        // Anything else (e.g. a system media controller) goes through the normal
        // MediaSessionService handshake.
        return if (intent?.action == ACTION_LOCAL_BIND) localBinder else super.onBind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val currentPlayer = player
        if (currentPlayer == null || !currentPlayer.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        super.onDestroy()
    }

    /** Tapping the media notification brings the app back to the front. */
    private fun openAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    companion object {
        const val ACTION_LOCAL_BIND = "com.toxa.pureradio.action.LOCAL_BIND"
        const val PREF_BEST_STREAM = "best_stream"
    }
}
