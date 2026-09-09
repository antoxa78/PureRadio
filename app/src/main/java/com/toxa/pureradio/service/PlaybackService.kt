package com.toxa.pureradio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.toxa.pureradio.R

/**
 * Hosts the [ExoPlayer] and [MediaSession] as a proper foreground service so that
 * playback survives the app leaving the foreground (screen off, Home button, the
 * Picture-in-Picture window being swiped away, etc.) and so the system shows a
 * standard media notification with lock-screen / notification-shade controls.
 *
 * The app's own UI (MainViewModel) binds to this service directly (see [LocalBinder])
 * to get a raw [ExoPlayer] reference — everything in the ViewModel that used to call
 * into a self-owned ExoPlayer keeps working unchanged, it just talks to the player
 * living here instead. [onGetSession] is what lets Media3's MediaSessionService
 * machinery observe playback state and automatically move this service in and out
 * of the foreground with a system-managed notification.
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
        createNotificationChannel()
        val prefs = getSharedPreferences("pure_radio_prefs", Context.MODE_PRIVATE)
        buildPlayer(
            extraBuffering = prefs.getBoolean("extra_buffering", false),
            audioPassthrough = prefs.getBoolean("audio_passthrough", false)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // playStation() starts us with startForegroundService(), which requires
        // startForeground() within the system's timeout. Media3 only promotes the
        // service to foreground once playback is genuinely active, so a slow or
        // failing radio stream could leave us outside that window and crash the app.
        // Calling startForeground() here immediately satisfies the requirement;
        // Media3 then updates the notification with playback controls once the
        // session is active.
        startForeground(
            NOTIFICATION_ID,
            placeholderNotification(intent?.getStringExtra(EXTRA_STATION_NAME))
        )
        return super.onStartCommand(intent, flags, startId)
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

        if (audioPassthrough) {
            val renderersFactory = DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableAudioFloatOutput(true)
            builder.setRenderersFactory(renderersFactory)
        }

        builder.setAudioAttributes(audioAttributes, true)
        builder.setHandleAudioBecomingNoisy(true)
        builder.setSkipSilenceEnabled(false)

        val newPlayer = builder.build().apply {
            // Force 1.0 speed and pitch to ensure no resampling for time-stretching
            playbackParameters = PlaybackParameters.DEFAULT
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
            mediaSession = MediaSession.Builder(this, newPlayer).build()
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.playback_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun placeholderNotification(stationName: String?): Notification {
        @Suppress("DEPRECATION")
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(stationName ?: getString(R.string.playback_notification_placeholder))
            .setContentText(getString(R.string.playback_notification_connecting))
            .setSmallIcon(applicationInfo.icon)
            .setPriority(Notification.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_LOCAL_BIND = "com.toxa.pureradio.action.LOCAL_BIND"
        const val EXTRA_STATION_NAME = "com.toxa.pureradio.extra.STATION_NAME"
        private const val CHANNEL_ID = "playback"
        private const val NOTIFICATION_ID = 1001
    }
}
