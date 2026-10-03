package com.toxa.pureradio

import android.os.Bundle
import android.net.Uri
import android.content.pm.PackageManager
import android.widget.Toast
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.graphics.drawable.Icon
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.app.Activity
import android.content.IntentFilter
import android.util.Rational
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.toxa.pureradio.BuildConfig
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Checkbox
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.rememberDrawerState
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import androidx.tv.material3.Switch
import coil.compose.AsyncImage
import com.toxa.pureradio.R
import com.toxa.pureradio.data.model.Station
import com.toxa.pureradio.network.Country
import com.toxa.pureradio.network.ServerStats
import com.toxa.pureradio.network.Tag
import com.toxa.pureradio.ui.theme.PureRadioTheme
import com.toxa.pureradio.ui.viewmodel.AppLanguage
import com.toxa.pureradio.ui.viewmodel.AppTheme
import com.toxa.pureradio.ui.viewmodel.BitrateFilter
import com.toxa.pureradio.ui.viewmodel.GenreGroup
import com.toxa.pureradio.ui.viewmodel.MainViewModel
import com.toxa.pureradio.ui.viewmodel.NavigationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val isInPipMode = mutableStateOf(false)
    // Shown at most once per process: reassures the user that leaving the app doesn't
    // stop playback even when Picture-in-Picture itself can't be entered (older devices,
    // PiP disabled by the user/OEM, etc.) — audio keeps going via PlaybackService's
    // foreground notification either way.
    private var pipFallbackNoticeShown = false

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_STOP_RADIO") {
                viewModel.stopPlayback()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            stopReceiver,
            IntentFilter("ACTION_STOP_RADIO"),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
        setContent {
            val isPip by isInPipMode
            val isInitialized by viewModel.isInitialized.collectAsState()
            var splashElapsed by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                // A brief minimum so the splash doesn't just flash by, but short enough
                // that it doesn't add a flat multi-second delay to every single launch —
                // isInitialized (from the ViewModel) still gates it on top of this.
                delay(1200)
                splashElapsed = true
            }
            val showSplash by remember { derivedStateOf { !isInitialized || !splashElapsed } }
            val appTheme by viewModel.appTheme.collectAsState()
            PureRadioTheme(theme = appTheme) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .onKeyEvent {
                            viewModel.resetScreensaverTimer()
                            false
                        },
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(
                        containerColor = if (isPip) Color.Transparent else MaterialTheme.colorScheme.background
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isPip) {
                            PipContent(viewModel)
                        } else if (showSplash) {
                            SplashScreen()
                        } else {
                            // Subtle background gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.background
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(x = 1000f, y = 0f),
                                            radius = 2000f
                                        )
                                    )
                            )
                            MainScreen(viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(stopReceiver) } catch (_: Exception) {}
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        updatePipParams()
    }

    private fun updatePipParams() {
        if (viewModel.isPlaying.value) {
            val station = viewModel.currentStation.value ?: return

            // PiP genuinely isn't available on this device/config (older API, feature
            // disabled by the OEM or the user, etc). Don't bother building params we
            // know will fail — just reassure the user once that audio keeps playing
            // via the notification instead of silently doing nothing.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                showPipFallbackNoticeOnce()
                return
            }
            val metadata = viewModel.mediaMetadata.value
            
            val stopIntent = PendingIntent.getBroadcast(
                this,
                1,
                Intent("ACTION_STOP_RADIO").setPackage(packageName),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val stopAction = RemoteAction(
                Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                "Close",
                "Stop Radio",
                stopIntent
            )
            
            val openIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            val openAction = RemoteAction(
                Icon.createWithResource(this, android.R.drawable.ic_menu_revert),
                "Open",
                "Open App",
                openIntent
            )

            val builder = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(239, 100))
                .setActions(listOf(openAction, stopAction))
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(true)
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val title = if (!metadata?.title.isNullOrEmpty()) metadata?.title.toString() else station.name
                builder.setTitle(title)
                builder.setSubtitle(station.name)
            }

            val params = builder.build()
            try {
                setPictureInPictureParams(params)
            } catch (e: Exception) {
                // Fallback for older versions if setPictureInPictureParams fails
                showPipFallbackNoticeOnce()
            }

            // For Android 14+ we call enterPictureInPictureMode manually if not auto-triggered
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                 try {
                     enterPictureInPictureMode(params)
                 } catch (e: Exception) {
                     showPipFallbackNoticeOnce()
                 }
            }
        }
    }

    /**
     * Lets the user know — once per process — that leaving the app is safe even when PiP
     * can't be entered: playback continues in the background via [PlaybackService]'s
     * notification, it just won't show the floating PiP window on top of whatever they
     * switch to.
     */
    private fun showPipFallbackNoticeOnce() {
        if (pipFallbackNoticeShown) return
        pipFallbackNoticeShown = true
        Toast.makeText(this, getString(R.string.pip_unavailable_notice), Toast.LENGTH_LONG).show()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PipContent(viewModel: MainViewModel) {
    val currentStation by viewModel.currentStation.collectAsState()
    val mediaMetadata by viewModel.mediaMetadata.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        currentStation?.let { station ->
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(54.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    AsyncImage(
                        model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit,
                        error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    val displayTitle = if (!mediaMetadata?.title.isNullOrEmpty()) {
                        mediaMetadata?.title.toString()
                    } else {
                        station.name
                    }
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.OpenInFull,
                        contentDescription = stringResource(R.string.content_desc_open),
                        modifier = Modifier.size(24.dp),
                        tint = readableAccent()
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.content_desc_close),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun SplashScreen() {
    val iconAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val iconScale = remember { androidx.compose.animation.core.Animatable(0.6f) }
    val glowAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val titleOffset = remember { androidx.compose.animation.core.Animatable(40f) }
    val titleAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val subAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow = infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        iconAlpha.animateTo(1f, animationSpec = tween(400))
        iconScale.animateTo(1f, animationSpec = tween(500, easing = FastOutLinearInEasing))
        glowAlpha.animateTo(1f, animationSpec = tween(700))
        titleOffset.animateTo(0f, animationSpec = tween(500, easing = FastOutLinearInEasing))
        titleAlpha.animateTo(1f, animationSpec = tween(300))
        delay(100)
        subAlpha.animateTo(1f, animationSpec = tween(400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF0A0E14)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer { alpha = glowAlpha.value * pulseGlow.value }
                        .background(
                            androidx.compose.ui.graphics.Color(0xFF00B0FF).copy(alpha = 0.12f),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer { alpha = glowAlpha.value * pulseGlow.value * 0.5f }
                        .background(
                            androidx.compose.ui.graphics.Color(0xFF00B0FF).copy(alpha = 0.08f),
                            CircleShape
                        )
                )
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.toxa.pureradio.R.drawable.ic_radio_logo),
                    contentDescription = "Pure Radio",
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            alpha = iconAlpha.value
                            scaleX = iconScale.value
                            scaleY = iconScale.value
                        },
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Text(
                    text = "Pure ",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    }
                )
                Text(
                    text = "Radio",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color(0xFF00B0FF),
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color(0xFFB0BEC5),
                modifier = Modifier.graphicsLayer { alpha = subAlpha.value }
            )
        }
    }
}

/**
 * Single source of truth for a [NavigationItem]'s localized display name — used by the
 * nav drawer, the page title, and the "Startup Category" settings picker, so all three
 * always show the same (translated) text instead of each keeping its own copy.
 */
@Composable
fun navigationItemLabel(item: NavigationItem): String = when (item) {
    NavigationItem.Home -> stringResource(R.string.nav_home)
    NavigationItem.Popular -> stringResource(R.string.nav_popular)
    NavigationItem.Recent -> stringResource(R.string.nav_recent)
    NavigationItem.Search -> stringResource(R.string.nav_search)
    NavigationItem.Genres -> stringResource(R.string.nav_genres)
    NavigationItem.Countries -> stringResource(R.string.nav_countries)
    NavigationItem.Favourites -> stringResource(R.string.nav_favourites)
    NavigationItem.Settings -> stringResource(R.string.nav_settings)
    NavigationItem.Exit -> stringResource(R.string.nav_exit)
}

@Composable
fun appThemeDisplayName(theme: AppTheme): String = when (theme) {
    AppTheme.ModernBlue -> stringResource(R.string.theme_modern_blue)
    AppTheme.RetroGold -> stringResource(R.string.theme_retro_gold)
    AppTheme.BlueNeon -> stringResource(R.string.theme_blue_neon)
    AppTheme.Violet -> stringResource(R.string.theme_violet)
    AppTheme.Monochrome -> stringResource(R.string.theme_monochrome)
    AppTheme.Forest -> stringResource(R.string.theme_forest)
    AppTheme.Contrast -> stringResource(R.string.theme_contrast)
}

@Composable
fun appThemeDescription(theme: AppTheme): String = when (theme) {
    AppTheme.ModernBlue -> stringResource(R.string.theme_modern_blue_desc)
    AppTheme.RetroGold -> stringResource(R.string.theme_retro_gold_desc)
    AppTheme.BlueNeon -> stringResource(R.string.theme_blue_neon_desc)
    AppTheme.Violet -> stringResource(R.string.theme_violet_desc)
    AppTheme.Monochrome -> stringResource(R.string.theme_monochrome_desc)
    AppTheme.Forest -> stringResource(R.string.theme_forest_desc)
    AppTheme.Contrast -> stringResource(R.string.theme_contrast_desc)
}

@Composable
fun appLanguageDisplayName(language: AppLanguage): String = when (language) {
    AppLanguage.English -> stringResource(R.string.language_english)
    AppLanguage.Russian -> stringResource(R.string.language_russian)
    AppLanguage.Ukrainian -> stringResource(R.string.language_ukrainian)
}

/** "classic rock" -> "Classic Rock". Used for genre / country / tag names everywhere. */
fun String.toTitleCase(): String =
    lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

/** WCAG contrast ratio between two colours (1.0 – 21.0). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/**
 * Accent colour that stays readable as text/icon colour on the app's dark surfaces.
 * Some themes (Modern Blue, Blue Neon) use a deep blue `primary` that is fine as a fill
 * but nearly invisible as text on the dark background, so fall back to the theme's
 * lighter tertiary/secondary tone (or plain onSurface) when `primary` is too dark.
 */
@Composable
fun readableAccent(): Color {
    val cs = MaterialTheme.colorScheme
    val bg = cs.surfaceVariant
    return listOf(cs.primary, cs.tertiary, cs.secondary)
        .firstOrNull { contrastRatio(it, bg) >= 4.5f } ?: cs.onSurface
}

/** [preferred] if it is readable on [background], otherwise [fallback]. */
fun readableOn(background: Color, preferred: Color, fallback: Color): Color =
    if (contrastRatio(preferred, background) >= 3f) preferred else fallback

/** Elapsed playback time as mm:ss, switching to h:mm:ss after the first hour. */
fun formatElapsed(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

/** Decoder line such as "AAC 128k 44kHz Stereo", shared by the Now Playing bar and screensaver. */
@OptIn(UnstableApi::class)
fun formatTechnicalInfo(format: androidx.media3.common.Format?, station: Station): String {
    if (format == null) return "${station.bitrate}k"
    val kbps = if (format.bitrate > 0) "${format.bitrate / 1000}k" else "${station.bitrate}k"
    val samplerate = if (format.sampleRate > 0) "${format.sampleRate / 1000}kHz" else ""
    val codec = format.sampleMimeType?.removePrefix("audio/")?.uppercase()
        ?.replace("MPEG", "MP3")
        ?.replace("MP4A-LATM", "AAC")
        ?: station.codec.orEmpty().uppercase()
    val channels = when (format.channelCount) {
        1 -> "Mono"
        2 -> "Stereo"
        in 3..8 -> "${format.channelCount}ch"
        else -> ""
    }
    return listOf(codec, kbps, samplerate, channels).filter { it.isNotBlank() }.joinToString(" ")
}

/** Short quality label for a station tile, e.g. "MP3 · 128k" (null when nothing is known). */
fun stationQualityLabel(station: Station): String? {
    val codec = station.codec.orEmpty().trim().uppercase().takeIf { it.isNotEmpty() && it != "UNKNOWN" }
    val bitrate = station.bitrate.takeIf { it > 0 }?.let { "${it}k" }
    val parts = listOfNotNull(codec, bitrate)
    return if (parts.isEmpty()) null else parts.joinToString(" · ")
}

/** Consistent section header for the Settings screen. */
@Composable
fun SettingsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    topPadding: androidx.compose.ui.unit.Dp = 24.dp
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = readableAccent(),
        modifier = modifier.padding(start = 12.dp, end = 12.dp, top = topPadding, bottom = 8.dp)
    )
}

/** The single "this option is selected" marker used by every settings picker. */
@Composable
fun SelectedCheck() {
    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = readableAccent())
}

/** Three-dot preview of a theme's colours, rendered with that theme's own colour scheme. */
@Composable
fun ThemeSwatch(theme: AppTheme) {
    PureRadioTheme(theme = theme) {
        val cs = MaterialTheme.colorScheme
        Surface(
            shape = CircleShape,
            colors = SurfaceDefaults.colors(containerColor = cs.background),
            border = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                shape = CircleShape
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(cs.primary, cs.primaryContainer, cs.surfaceVariant).forEach { color ->
                    Box(modifier = Modifier.size(14.dp).background(color, CircleShape))
                }
            }
        }
    }
}

/** Centered icon + title + hint, shown when a list has nothing to display. */
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = readableAccent())
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.width(460.dp)
            )
        }
    }
}

/**
 * Non-blocking message card shown at the bottom of the screen (errors, confirmations,
 * reconnect notices). Unlike the old full-screen overlay it never hides or dims the
 * content behind it, so the user keeps their place in the grid.
 */
@Composable
fun MessageToast(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    iconTint: Color,
    accentColor: Color,
    showProgress: Boolean = false
) {
    Surface(
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 6.dp,
        border = androidx.tv.material3.Border(
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
            shape = MaterialTheme.shapes.medium
        ),
        modifier = Modifier.padding(horizontal = 32.dp).widthIn(max = 720.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showProgress) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = iconTint
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedNavItem by viewModel.selectedNavItem.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val genreGroups by viewModel.genreGroups.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isInitialized by viewModel.isInitialized.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val lastBrowsedCategory by viewModel.lastBrowsedCategory.collectAsState()
    val selectedSearchTag by viewModel.selectedSearchTag.collectAsState()
    val selectedBitrates by viewModel.selectedBitrates.collectAsState()
    val hasMoreStations by viewModel.hasMoreStations.collectAsState()
    val settingsSubMenu by viewModel.settingsSubMenu.collectAsState()
    val playbackTime by viewModel.playbackTime.collectAsState()
    val isScreensaverShowing by viewModel.isScreensaverShowing.collectAsState()
    val visibleGenres by viewModel.visibleGenres.collectAsState()
    val filteredTags by viewModel.filteredTags.collectAsState()
    val tagSearchQuery by viewModel.tagSearchQuery.collectAsState()
    val genreSortMode by viewModel.genreSortMode.collectAsState()
    val mediaMetadata by viewModel.mediaMetadata.collectAsState()
    val audioFormat by viewModel.audioFormat.collectAsState()
    val filePickerState by viewModel.filePickerState.collectAsState()
    val pendingImportStations by viewModel.pendingImportStations.collectAsState()
    val pendingHomeSettings by viewModel.pendingHomeSettings.collectAsState()
    val pendingOverwriteFile by viewModel.pendingOverwriteFile.collectAsState()
    val quitConfirmationEnabled by viewModel.quitConfirmationEnabled.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFavoritesFromM3u(it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportFavoritesToM3u(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    // Needed to show the playback notification (lock-screen / notification-shade controls)
    // from the foreground playback service on Android 13+. Playback itself doesn't depend
    // on this being granted — only the visible notification does.
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, notificationPermission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(notificationPermission)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(
                        android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        context.startActivity(intent)
                    } catch (e2: Exception) {}
                }
            }
        } else {
            val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            delay(5000)
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(4000)
            viewModel.clearSuccess()
        }
    }

    var stationToFavorite by remember { mutableStateOf<Station?>(null) }
    var isDialogReady by remember { mutableStateOf(false) }
    var genreToAdd by remember { mutableStateOf<Tag?>(null) }
    var genreToRemove by remember { mutableStateOf<String?>(null) }
    var isGenreDialogReady by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerFocusRequesters = remember { NavigationItem.entries.associateWith { FocusRequester() } }
    val dialogFocusRequester = remember { FocusRequester() }
    val cancelFocusRequester = remember { FocusRequester() }

    val isDrawerOpen = drawerState.currentValue == DrawerValue.Open
    val drawerOpenedIntentionally = remember { mutableStateOf(false) }
    BackHandler {
        if (isDrawerOpen) {
            drawerState.setValue(DrawerValue.Closed)
        } else {
            when {
                error != null -> viewModel.cancelRetry()
                pendingImportStations != null -> viewModel.cancelRestore()
                pendingOverwriteFile != null -> viewModel.cancelOverwrite()
                filePickerState != null -> viewModel.closeFilePicker()
                settingsSubMenu != null -> viewModel.setSettingsSubMenu(null)
                selectedTag != null -> viewModel.selectTag(null)
                selectedCountry != null -> viewModel.selectCountry(null)
                selectedSearchTag != null -> viewModel.selectSearchTag(null)
                selectedNavItem == NavigationItem.Search && (searchQuery.isNotEmpty() || stations.isNotEmpty()) -> viewModel.clearSearch()
                isInitialized -> {
                    drawerOpenedIntentionally.value = true
                    drawerState.setValue(DrawerValue.Open)
                    try { drawerFocusRequesters[selectedNavItem]?.requestFocus() } catch (_: Exception) {}
                }
            }
        }
    }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Open) {
            try { drawerFocusRequesters[selectedNavItem]?.requestFocus() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(stationToFavorite) {
        if (stationToFavorite != null) {
            isDialogReady = false
            try { cancelFocusRequester.requestFocus() } catch (_: Exception) {}
            delay(800)
            isDialogReady = true
        }
    }

    LaunchedEffect(genreToAdd, genreToRemove) {
        if (genreToAdd != null || genreToRemove != null) {
            isGenreDialogReady = false
            try { cancelFocusRequester.requestFocus() } catch (_: Exception) {}
            delay(800)
            isGenreDialogReady = true
        }
    }

    // Clear drawerOpenedIntentionally after one frame if drawer didn't consume it
    LaunchedEffect(drawerOpenedIntentionally.value) {
        if (drawerOpenedIntentionally.value) {
            withFrameNanos { }
            drawerOpenedIntentionally.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavigationDrawer(
            drawerState = drawerState,
            drawerContent = { drawerValue ->
                val isDrawerCurrentlyOpen = drawerValue == DrawerValue.Open
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .onFocusChanged {
                            if (!it.hasFocus && drawerValue == DrawerValue.Open) {
                                drawerState.setValue(DrawerValue.Closed)
                            }
                        },
                    contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp, start = 4.dp, end = 4.dp)
                ) {
                    items(NavigationItem.entries) { item ->
                        NavigationDrawerItem(
                            selected = selectedNavItem == item,
                            onClick = { 
                                if (item == NavigationItem.Exit) {
                                    drawerState.setValue(DrawerValue.Closed)
                                    if (quitConfirmationEnabled) {
                                        showExitDialog = true
                                    } else {
                                        viewModel.stopPlayback()
                                        (context as? Activity)?.finish()
                                    }
                                } else {
                                    viewModel.selectNavigationItem(item)
                                    drawerState.setValue(DrawerValue.Closed)
                                }
                            },
                            colors = androidx.tv.material3.NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                // onPrimaryContainer is dark blue on dark blue in the blue themes,
                                // which made the selected entry unreadable — fall back to onSurface.
                                selectedContentColor = readableOn(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                    MaterialTheme.colorScheme.onSurface
                                )
                            ),
                            modifier = Modifier
                                .focusProperties { canFocus = isDrawerCurrentlyOpen }
                                .then(if (isDrawerCurrentlyOpen) Modifier.focusRequester(drawerFocusRequesters[item]!!) else Modifier),
                            leadingContent = {
                                val icon = when (item) {
                                    NavigationItem.Home -> Icons.Default.Home
                                    NavigationItem.Popular -> Icons.Default.Whatshot
                                    NavigationItem.Recent -> Icons.Default.History
                                    NavigationItem.Search -> Icons.Default.Search
                                    NavigationItem.Genres -> Icons.Default.Category
                                    NavigationItem.Countries -> Icons.Default.Public
                                    NavigationItem.Favourites -> Icons.Default.Favorite
                                    NavigationItem.Settings -> Icons.Default.Settings
                                    NavigationItem.Exit -> Icons.AutoMirrored.Filled.ExitToApp
                                }
                                Icon(
                                    icon, 
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        ) {
                            val label = navigationItemLabel(item)
                            Text(
                                label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val title = selectedTag?.name?.toTitleCase()
                        ?: selectedCountry?.name?.toTitleCase()
                        ?: navigationItemLabel(selectedNavItem)
                val isDeepDive = selectedTag != null || selectedCountry != null
                // Station count sits next to the title (muted) instead of being glued into it,
                // and is shown wherever the full list is local: category deep dives,
                // Favourites and Recent.
                val showCount = stations.isNotEmpty() && (isDeepDive ||
                        selectedNavItem == NavigationItem.Favourites ||
                        selectedNavItem == NavigationItem.Recent)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDeepDive) {
                        Surface(
                            colors = SurfaceDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = if (selectedTag != null) stringResource(R.string.genre_badge) else stringResource(R.string.country_badge),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = if (isDeepDive) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (showCount) {
                            Text(
                                text = stringResource(R.string.count_stations_plain, stations.size),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                            )
                        }
                    }

                val showBitrateFilters = (selectedNavItem == NavigationItem.Home) ||
                                        (selectedNavItem == NavigationItem.Popular) ||
                                        (selectedNavItem == NavigationItem.Genres) ||
                                        (selectedNavItem == NavigationItem.Countries)

                    if (showBitrateFilters) {
                        BitrateFilters(
                            selectedBitrates = selectedBitrates,
                            onToggleFilter = { viewModel.toggleBitrateFilter(it) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        when (selectedNavItem) {
                            NavigationItem.Home -> {
                                if (genreGroups.isNotEmpty()) {
                                    if (selectedTag == null && selectedCountry == null) {
                                        key("home_groups") {
                                            GenreGroupGrid(
                                                groups = genreGroups,
                                                targetCategoryName = lastBrowsedCategory,
                                                onGroupClick = { name ->
                                                    if (genreToAdd == null && genreToRemove == null && stationToFavorite == null) {
                                                        val group = genreGroups.find { it.genreName == name }
                                                        if (group?.isCountry == true) {
                                                            val country = countries.find { it.name == name }
                                                                ?: Country(name = name, iso_3166_1 = "", stationcount = 0)
                                                            viewModel.selectCountry(country)
                                                        } else {
                                                            val tag = tags.find { it.name == name }
                                                                ?: Tag(name = name, stationcount = group?.totalStations ?: 0)
                                                            viewModel.selectTag(tag)
                                                        }
                                                    }
                                                },
                                                onGroupLongClick = { name ->
                                                    val group = genreGroups.find { it.genreName == name }
                                                    if (group?.isCountry == false) {
                                                        genreToRemove = name
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        key(selectedTag?.name ?: selectedCountry?.name ?: "home_stations") {
                                            // The station count is shown next to the page title.
                                            StationGrid(
                                                stations = stations,
                                                viewModel = viewModel,
                                                isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null,
                                                onLoadMore = if (hasMoreStations) { { viewModel.loadMoreStations() } } else null,
                                                onLongClick = { stationToFavorite = it }
                                            )
                                        }
                                    }
                                } else {
                                    key("home_top") {
                                        StationGrid(
                                            stations = stations,
                                            viewModel = viewModel,
                                            isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null,
                                            onLoadMore = if (hasMoreStations) { { viewModel.loadMoreStations() } } else null,
                                            onLongClick = { stationToFavorite = it }
                                        )
                                    }
                                }
                            }
                            NavigationItem.Popular -> {
                                key("popular_stations") {
                                    StationGrid(
                                        stations = stations,
                                        viewModel = viewModel,
                                        isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null,
                                        onLoadMore = if (hasMoreStations) { { viewModel.loadMoreStations() } } else null,
                                        onLongClick = { stationToFavorite = it }
                                    )
                                }
                            }
                            NavigationItem.Recent -> {
                                key("recent_stations") {
                                    StationGrid(stations, viewModel, isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null) { stationToFavorite = it }
                                }
                            }
                            NavigationItem.Favourites -> {
                                key("favorite_stations") {
                                    StationGrid(stations, viewModel, isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null) { stationToFavorite = it }
                                }
                            }
                            NavigationItem.Search -> {
                                SearchScreen(
                                    viewModel,
                                    onLongClick = { stationToFavorite = it },
                                    onTagGroupLongClick = { tagName ->
                                        if (visibleGenres.contains(tagName)) {
                                            genreToRemove = tagName
                                        } else {
                                            genreToAdd = tags.find { it.name.equals(tagName, ignoreCase = true) }
                                                ?: Tag(name = tagName, stationcount = 0)
                                        }
                                    },
                                    isGenreDialogOpen = genreToAdd != null || genreToRemove != null || stationToFavorite != null
                                )
                            }
                            NavigationItem.Genres -> {
                                    if (selectedTag == null) {
                                    key("genres_list") {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            var localTagSearchQuery by remember { mutableStateOf(tagSearchQuery) }
                                            LaunchedEffect(tagSearchQuery) {
                                                if (tagSearchQuery != localTagSearchQuery) {
                                                    localTagSearchQuery = tagSearchQuery
                                                }
                                            }
                                            val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 32.dp, top = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                            OutlinedTextField(
                                                value = localTagSearchQuery,
                                                onValueChange = { localTagSearchQuery = it; viewModel.setTagSearchQuery(it) },
                                                label = { Text(stringResource(R.string.search_genres_hint)) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                                trailingIcon = {
                                                    if (localTagSearchQuery.isNotEmpty()) {
                                                        androidx.tv.material3.Button(
                                                            onClick = { viewModel.setTagSearchQuery(""); localTagSearchQuery = "" },
                                                            modifier = Modifier.size(36.dp),
                                                            contentPadding = PaddingValues(0.dp),
                                                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                                                containerColor = Color.Transparent,
                                                                contentColor = Color.White
                                                            )
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = stringResource(R.string.content_desc_clear),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                colors = TextFieldDefaults.colors(
                                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    focusedLabelColor = readableAccent(),
                                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent
                                                )
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(stringResource(R.string.sort_label), style = MaterialTheme.typography.labelLarge)
                                                com.toxa.pureradio.ui.viewmodel.GenreSortMode.entries.forEach { mode ->
                                                    Button(
                                                        onClick = { viewModel.setGenreSortMode(mode) },
                                                        modifier = Modifier.padding(horizontal = 4.dp),
                                                        colors = if (genreSortMode == mode) {
                                                            androidx.tv.material3.ButtonDefaults.colors(
                                                                containerColor = MaterialTheme.colorScheme.primary,
                                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                                            )
                                                        } else {
                                                            androidx.tv.material3.ButtonDefaults.colors()
                                                        }
                                                    ) {
                                                        Text(if (mode == com.toxa.pureradio.ui.viewmodel.GenreSortMode.Name) stringResource(R.string.sort_name) else stringResource(R.string.sort_count))
                                                    }
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TagGrid(
                                                    tags = filteredTags,
                                                    targetTagName = lastBrowsedCategory,
                                                    homeGenres = visibleGenres,
                                                    onTagClick = { 
                                                        if (genreToAdd == null && genreToRemove == null && stationToFavorite == null) {
                                                            viewModel.selectTag(it)
                                                        }
                                                    },
                                                    onTagLongClick = { tag ->
                                                        genreToAdd = tag
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    key(selectedTag!!.name) {
                                        StationGrid(
                                            stations = stations,
                                            viewModel = viewModel,
                                            isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null,
                                            onLoadMore = if (hasMoreStations) { { viewModel.loadMoreStations() } } else null,
                                            onLongClick = { stationToFavorite = it }
                                        )
                                    }
                                }
                            }
                            NavigationItem.Countries -> {
                                if (selectedCountry == null) {
                                    key("countries_list") {
                                        CountryGrid(
                                            countries = countries,
                                            targetCountryName = lastBrowsedCategory,
                                            onCountryClick = { viewModel.selectCountry(it) }
                                        )
                                    }
                                } else {
                                    key(selectedCountry!!.name) {
                                        StationGrid(
                                            stations = stations,
                                            viewModel = viewModel,
                                            isLongClickActive = stationToFavorite != null || genreToAdd != null || genreToRemove != null,
                                            onLoadMore = if (hasMoreStations) { { viewModel.loadMoreStations() } } else null,
                                            onLongClick = { stationToFavorite = it }
                                        )
                                    }
                                }
                            }
                            NavigationItem.Settings -> {
                                SettingsScreen(
                                    viewModel, 
                                    onImportPlaylist = { 
                                        try {
                                            importLauncher.launch("*/*") 
                                        } catch (e: Exception) {
                                            viewModel.setError("System picker unavailable. Using Internal Explorer.")
                                            viewModel.openFilePicker(isExport = false)
                                        }
                                    },
                                    onExportPlaylist = { 
                                        val fileName = viewModel.getTimestampedBackupFileName()
                                        try {
                                            exportLauncher.launch(fileName) 
                                        } catch (e: Exception) {
                                            viewModel.setError("System picker unavailable. Using Internal Explorer.")
                                            viewModel.openFilePicker(isExport = true, suggestedFileName = fileName)
                                        }
                                    },
                                    onPermissionRequest = {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                            if (!android.os.Environment.isExternalStorageManager()) {
                                                try {
                                                    val intent = Intent(
                                                        android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                                        Uri.parse("package:${context.packageName}")
                                                    )
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    try {
                                                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                                        context.startActivity(intent)
                                                    } catch (e2: Exception) {}
                                                }
                                            } else {
                                                viewModel.setSuccess("All Files Access already granted")
                                            }
                                        } else {
                                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                        }
                                    }
                                )
                            }
                            NavigationItem.Exit -> {}
                        }

                    if (isLoading && selectedNavItem != NavigationItem.Search) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = readableAccent(),
                                trackColor = Color.Transparent
                            )
                        }
                    }
                }
                
                // Add spacer to push content above NowPlayingBar when it's present
                if (currentStation != null) {
                    Spacer(modifier = Modifier.height(115.dp))
                }
            }

            // Errors, confirmations and reconnect notices share one non-blocking stack of
            // toasts at the bottom, above the Now Playing bar. The content behind stays
            // visible and keeps focus (the old error/success overlay blanked the screen).
            if (error != null || successMessage != null || infoMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (currentStation != null) 131.dp else 24.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        infoMessage?.let {
                            MessageToast(
                                text = it,
                                icon = null,
                                iconTint = readableAccent(),
                                accentColor = MaterialTheme.colorScheme.secondary,
                                showProgress = true
                            )
                        }
                        error?.let {
                            MessageToast(
                                text = it,
                                icon = Icons.Default.ErrorOutline,
                                iconTint = MaterialTheme.colorScheme.error,
                                accentColor = MaterialTheme.colorScheme.error
                            )
                        }
                        successMessage?.let {
                            MessageToast(
                                text = it,
                                icon = Icons.Default.CheckCircle,
                                iconTint = readableAccent(),
                                accentColor = readableAccent()
                            )
                        }
                    }
                }
            }
        }
    }

        // Overlay NowPlayingBar at the bottom of the screen
        currentStation?.let { station ->
            val playbackDuration by viewModel.playbackDuration.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                NowPlayingBar(
                    station = station,
                    isPlaying = isPlaying,
                    isFavorite = viewModel.isFavorite(station),
                    playbackTime = playbackTime,
                    playbackDuration = playbackDuration,
                    mediaMetadata = mediaMetadata,
                    audioFormat = audioFormat,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onToggleFavorite = { viewModel.toggleFavorite(station) },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() }
                )
            }
        }
    }

    if (isScreensaverShowing) {
        Screensaver(viewModel)
    }

    filePickerState?.let { state ->
        FilePicker(
            state = state,
            onNavigate = { viewModel.navigateInFilePicker(it) },
            onNavigateUp = { viewModel.navigateUpFilePicker() },
            onSelected = { viewModel.handleFileSelection(it) },
            onDismiss = { viewModel.closeFilePicker() }
        )
    }

    pendingOverwriteFile?.let { file ->
        val overwriteFocusRequester = remember { FocusRequester() }
        Dialog(onDismissRequest = { viewModel.cancelOverwrite() }) {
            Surface(
                modifier = Modifier.width(420.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.tv.material3.Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.extraLarge
                )
            ) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.dialog_overwrite_backup), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.overwrite_dialog_message, file.name),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { viewModel.cancelOverwrite() },
                            modifier = Modifier.weight(1f).focusRequester(overwriteFocusRequester)
                        ) {
                            Text(stringResource(R.string.action_no), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = { viewModel.confirmOverwrite() },
                            modifier = Modifier.weight(1f),
                            colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.action_yes), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    LaunchedEffect(Unit) {
                        try { overwriteFocusRequester.requestFocus() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    stationToFavorite?.let { station ->
        val isAlreadyFavorite = viewModel.isFavorite(station)
        Dialog(onDismissRequest = { stationToFavorite = null }) {
            Surface(
                modifier = Modifier.width(420.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.tv.material3.Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.extraLarge
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isAlreadyFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = readableAccent()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isAlreadyFavorite) stringResource(R.string.favorite_remove_title) else stringResource(R.string.favorite_add_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { if (isDialogReady) stationToFavorite = null },
                            modifier = Modifier.weight(1f).focusRequester(cancelFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(R.string.action_cancel_caps), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = {
                                if (isDialogReady) {
                                    viewModel.toggleFavorite(station)
                                    stationToFavorite = null
                                }
                            },
                            modifier = Modifier.weight(1f).focusRequester(dialogFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = if (isAlreadyFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                contentColor = if (isAlreadyFavorite) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                if (isAlreadyFavorite) stringResource(R.string.action_remove_caps) else stringResource(R.string.action_add_caps),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    genreToAdd?.let { tag ->
        val addFocusRequester = remember { FocusRequester() }
        val backFocusRequester = remember { FocusRequester() }
        Dialog(onDismissRequest = { genreToAdd = null }) {
            Surface(
                modifier = Modifier.width(420.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.tv.material3.Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.extraLarge
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = readableAccent()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.dialog_add_home),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = tag.name.toTitleCase(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { if (isGenreDialogReady) genreToAdd = null },
                            modifier = Modifier.weight(1f).focusRequester(backFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(R.string.action_back_caps), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = {
                                if (isGenreDialogReady) {
                                    viewModel.toggleGenreVisibility(tag.name)
                                    genreToAdd = null
                                }
                            },
                            modifier = Modifier.weight(1f).focusRequester(addFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                stringResource(R.string.action_add_caps),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        try { addFocusRequester.requestFocus() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    genreToRemove?.let { genreName ->
        val removeFocusRequester = remember { FocusRequester() }
        val backFocusRequester = remember { FocusRequester() }
        Dialog(onDismissRequest = { genreToRemove = null }) {
            Surface(
                modifier = Modifier.width(420.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.tv.material3.Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.extraLarge
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = readableAccent()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.dialog_remove_home),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = genreName.toTitleCase(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { if (isGenreDialogReady) genreToRemove = null },
                            modifier = Modifier.weight(1f).focusRequester(backFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(R.string.action_back_caps), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = {
                                if (isGenreDialogReady) {
                                    viewModel.toggleGenreVisibility(genreName)
                                    genreToRemove = null
                                }
                            },
                            modifier = Modifier.weight(1f).focusRequester(removeFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(
                                stringResource(R.string.action_remove_caps),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        try { removeFocusRequester.requestFocus() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        val exitYesFocusRequester = remember { FocusRequester() }
        val exitNoFocusRequester = remember { FocusRequester() }
        Dialog(onDismissRequest = { showExitDialog = false }) {
            Surface(
                modifier = Modifier.width(420.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.tv.material3.Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.extraLarge
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = readableAccent()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.exit_dialog_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { showExitDialog = false },
                            modifier = Modifier.weight(1f).focusRequester(exitNoFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(R.string.action_no_caps), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = {
                                viewModel.stopPlayback()
                                (context as? Activity)?.finish()
                            },
                            modifier = Modifier.weight(1f).focusRequester(exitYesFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(stringResource(R.string.action_yes_caps), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                    LaunchedEffect(Unit) {
                        try { exitNoFocusRequester.requestFocus() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    pendingImportStations?.let { stations ->
        var showConfirmation by remember { mutableStateOf(false) }
        val restoreFocusRequester = remember { FocusRequester() }

        if (!showConfirmation) {
            Dialog(onDismissRequest = { viewModel.cancelRestore() }) {
                Surface(
                    modifier = Modifier.width(420.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.tv.material3.Border(
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(48.dp), tint = readableAccent())
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(stringResource(R.string.settings_restore_favs), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        val homeCategoryCount = (pendingHomeSettings?.genres?.size ?: 0) + (pendingHomeSettings?.countries?.size ?: 0)
                        Text(
                            stringResource(
                                if (pendingHomeSettings != null) R.string.restore_dialog_found_items
                                else R.string.restore_dialog_found_stations,
                                stations.size,
                                homeCategoryCount
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Button(
                            onClick = { viewModel.confirmRestore(replace = false) },
                            modifier = Modifier.fillMaxWidth().focusRequester(restoreFocusRequester)
                        ) {
                            Text(stringResource(R.string.restore_action_add), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showConfirmation = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.restore_action_replace), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.cancelRestore() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.action_cancel), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        
                        LaunchedEffect(Unit) {
                            try { restoreFocusRequester.requestFocus() } catch (_: Exception) {}
                        }
                    }
                }
            }
        } else {
            Dialog(onDismissRequest = { showConfirmation = false }) {
                Surface(
                    modifier = Modifier.width(420.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.tv.material3.Border(
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(stringResource(R.string.restore_confirm_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.restore_confirm_message),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { showConfirmation = false },
                                modifier = Modifier.weight(1f).focusRequester(restoreFocusRequester)
                            ) {
                                Text(stringResource(R.string.action_no), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                            Button(
                                onClick = { viewModel.confirmRestore(replace = true) },
                                modifier = Modifier.weight(1f),
                                colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.action_yes), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                        
                        LaunchedEffect(Unit) {
                            try { restoreFocusRequester.requestFocus() } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BitrateFilters(
    selectedBitrates: Set<BitrateFilter>,
    onToggleFilter: (BitrateFilter) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.bitrate_filter_label), style = MaterialTheme.typography.labelLarge)
        BitrateFilter.entries.forEach { filter ->
            val label = when (filter) {
                BitrateFilter.Low -> stringResource(R.string.bitrate_low)
                BitrateFilter.High -> stringResource(R.string.bitrate_high)
                BitrateFilter.FLAC -> stringResource(R.string.bitrate_flac)
            }
            val isSelected = selectedBitrates.contains(filter)
            Button(
                onClick = { onToggleFilter(filter) },
                modifier = Modifier.padding(horizontal = 4.dp),
                scale = if (isSelected) androidx.tv.material3.ButtonDefaults.scale(focusedScale = 1.1f) else androidx.tv.material3.ButtonDefaults.scale(),
                colors = if (isSelected) {
                    androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    androidx.tv.material3.ButtonDefaults.colors()
                }
            ) {
                Text(label)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel, 
    onImportPlaylist: () -> Unit,
    onExportPlaylist: () -> Unit,
    onPermissionRequest: () -> Unit
) {
    androidx.compose.ui.platform.LocalContext.current
    val visibleGenres by viewModel.visibleGenres.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val filteredTags by viewModel.filteredTags.collectAsState()
    val tagSearchQuery by viewModel.tagSearchQuery.collectAsState()
    var localTagSearchQuery by remember { mutableStateOf(tagSearchQuery) }
    LaunchedEffect(tagSearchQuery) {
        if (tagSearchQuery != localTagSearchQuery) {
            localTagSearchQuery = tagSearchQuery
        }
    }
    val settingsSubMenu by viewModel.settingsSubMenu.collectAsState()
    val hideBroken by viewModel.hideBrokenStations.collectAsState()
    val serverStats by viewModel.serverStats.collectAsState()
    val lastUpdate by viewModel.lastDbUpdate.collectAsState()
    val autoUpdateInterval by viewModel.autoUpdateInterval.collectAsState()
    val screensaverEnabled by viewModel.screensaverEnabled.collectAsState()
    val screensaverTimeout by viewModel.screensaverTimeout.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val audioPassthrough by viewModel.audioPassthrough.collectAsState()
    val genreSortMode by viewModel.genreSortMode.collectAsState()
    val minTagFilter by viewModel.minTagFilter.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val quitConfirmationEnabled by viewModel.quitConfirmationEnabled.collectAsState()
    val autoReconnectEnabled by viewModel.autoReconnectEnabled.collectAsState()
    val extraBufferingEnabled by viewModel.extraBufferingEnabled.collectAsState()
    val defaultCategory by viewModel.defaultCategory.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val subMenuFocusRequester = remember { FocusRequester() }
    val mainMenuFocusRequester = remember { FocusRequester() }

    LaunchedEffect(settingsSubMenu) {
        if (settingsSubMenu != null) {
            try { subMenuFocusRequester.requestFocus() } catch (_: Exception) {}
        } else {
            try { mainMenuFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    when (settingsSubMenu) {
        "HomeGenres" -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_personalize_home), style = MaterialTheme.typography.headlineMedium)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(stringResource(R.string.sort_label), style = MaterialTheme.typography.labelLarge)
                        com.toxa.pureradio.ui.viewmodel.GenreSortMode.entries.forEach { mode ->
                            Button(
                                onClick = { viewModel.setGenreSortMode(mode) },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = if (genreSortMode == mode) {
                                    androidx.tv.material3.ButtonDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    androidx.tv.material3.ButtonDefaults.colors()
                                }
                            ) {
                                Text(if (mode == com.toxa.pureradio.ui.viewmodel.GenreSortMode.Name) stringResource(R.string.sort_name) else stringResource(R.string.sort_count))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                    OutlinedTextField(
                        value = localTagSearchQuery,
                        onValueChange = { viewModel.setTagSearchQuery(it); localTagSearchQuery = it },
                        label = { Text(stringResource(R.string.search_genres_hint)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = readableAccent(),
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
                
                item {
                    Text(stringResource(R.string.genres_section_header), style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        color = readableAccent())
                }

                items(filteredTags) { tag ->
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleGenreVisibility(tag.name) },
                        headlineContent = {
                            Text(tag.name.toTitleCase())
                        },
                        supportingContent = {
                            Text(stringResource(R.string.count_stations_plain, tag.stationcount))
                        },
                        trailingContent = {
                            Checkbox(checked = visibleGenres.contains(tag.name), onCheckedChange = null)
                        }
                    )
                }
                
            }
        }
        "AutoUpdate" -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_db_update_interval), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                val options = listOf(
                    0 to R.string.settings_bg_sync_desc_off,
                    12 to R.string.settings_bg_sync_desc_12,
                    24 to R.string.settings_bg_sync_desc_24
                )
                options.forEachIndexed { index, (hours, labelRes) ->
                    item(key = hours) {
                        ListItem(
                            selected = autoUpdateInterval == hours,
                            onClick = { 
                                viewModel.setAutoUpdateInterval(hours)
                                viewModel.setSettingsSubMenu(null)
                            },
                            modifier = if (index == 0) Modifier.focusRequester(subMenuFocusRequester) else Modifier,
                            headlineContent = { Text(stringResource(labelRes)) },
                            trailingContent = {
                                if (autoUpdateInterval == hours) {
                                    SelectedCheck()
                                }
                            }
                        )
                    }
                }
            }
        }
        "Screensaver" -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_screensaver_prefs), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleScreensaver(!screensaverEnabled) },
                        modifier = Modifier.focusRequester(subMenuFocusRequester),
                        headlineContent = { Text(stringResource(R.string.settings_activate_screensaver)) },
                        supportingContent = { Text(stringResource(R.string.settings_activate_screensaver_desc)) },
                        trailingContent = {
                            Switch(checked = screensaverEnabled, onCheckedChange = null)
                        }
                    )
                }
                item {
                    SettingsSectionHeader(stringResource(R.string.settings_display_mode))
                }
                val modes = listOf(
                    com.toxa.pureradio.ui.viewmodel.ScreensaverMode.StationInfo to R.string.screensaver_mode_station_info,
                    com.toxa.pureradio.ui.viewmodel.ScreensaverMode.BlackScreen to R.string.screensaver_mode_black_screen
                )
                items(modes) { (mode, labelRes) ->
                    ListItem(
                        selected = screensaverMode == mode,
                        onClick = { viewModel.setScreensaverMode(mode) },
                        headlineContent = { Text(stringResource(labelRes)) },
                        trailingContent = {
                            if (screensaverMode == mode) {
                                SelectedCheck()
                            }
                        }
                    )
                }
                item {
                    SettingsSectionHeader(stringResource(R.string.settings_idle_timeout))
                }
                val timeouts = listOf(1, 5, 10, 20, 30)
                items(timeouts) { minutes ->
                    ListItem(
                        selected = screensaverTimeout == minutes,
                        onClick = {
                            viewModel.setScreensaverTimeout(minutes)
                            viewModel.setSettingsSubMenu(null)
                        },
                        headlineContent = { Text(stringResource(R.string.minutes_format, minutes)) },
                        trailingContent = {
                            if (screensaverTimeout == minutes) {
                                SelectedCheck()
                            }
                        }
                    )
                }
            }
        }
        "AppTheme" -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_app_theme), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(AppTheme.entries) { theme ->
                    val label = appThemeDisplayName(theme)
                    val desc = appThemeDescription(theme)
                    ListItem(
                        selected = appTheme == theme,
                        onClick = { viewModel.setAppTheme(theme) },
                        headlineContent = { Text(label) },
                        supportingContent = { Text(desc) },
                        leadingContent = { ThemeSwatch(theme) },
                        trailingContent = {
                            if (appTheme == theme) {
                                SelectedCheck()
                            }
                        }
                    )
                }
            }
        }
        "AppLanguage" -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_app_language), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(AppLanguage.entries) { language ->
                    ListItem(
                        selected = appLanguage == language,
                        onClick = { viewModel.setAppLanguage(language) },
                        headlineContent = { Text(appLanguageDisplayName(language)) },
                        trailingContent = {
                            if (appLanguage == language) {
                                SelectedCheck()
                            }
                        }
                    )
                }
            }
        }
        "DefaultCategory" -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_startup_category), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                val categories = NavigationItem.entries.filter { it != NavigationItem.Settings && it != NavigationItem.Exit }
                items(categories) { item ->
                    ListItem(
                        selected = defaultCategory == item,
                        onClick = { 
                            viewModel.setDefaultCategory(item)
                            viewModel.setSettingsSubMenu(null)
                        },
                        headlineContent = { Text(navigationItemLabel(item)) },
                        trailingContent = {
                            if (defaultCategory == item) {
                                SelectedCheck()
                            }
                        }
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 170.dp)
            ) {
                // The page header above already says "Settings", so the list starts straight
                // with its first section.
                item { SettingsSectionHeader(stringResource(R.string.settings_section_general), topPadding = 8.dp) }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("AppTheme") },
                        modifier = Modifier.focusRequester(mainMenuFocusRequester),
                        headlineContent = { Text(stringResource(R.string.settings_app_theme)) },
                        supportingContent = {
                            Text(stringResource(R.string.current_value_format, appThemeDisplayName(appTheme)))
                        },
                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ThemeSwatch(appTheme)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("AppLanguage") },
                        headlineContent = { Text(stringResource(R.string.settings_app_language)) },
                        supportingContent = { Text(stringResource(R.string.current_value_format, appLanguageDisplayName(appLanguage))) },
                        leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("DefaultCategory") },
                        headlineContent = { Text(stringResource(R.string.settings_startup_category)) },
                        supportingContent = { Text(stringResource(R.string.currently_value_format, navigationItemLabel(defaultCategory))) },
                        leadingContent = { Icon(Icons.Default.Home, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("HomeGenres") },
                        headlineContent = { Text(stringResource(R.string.settings_home_curation)) },
                        supportingContent = { Text(stringResource(R.string.settings_home_curation_desc)) },
                        leadingContent = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("Screensaver") },
                        headlineContent = { Text(stringResource(R.string.settings_ambient_screensaver)) },
                        supportingContent = {
                            val label = if (screensaverEnabled) stringResource(R.string.settings_ambient_screensaver_desc_enabled, screensaverTimeout) else stringResource(R.string.settings_ambient_screensaver_desc_disabled)
                            Text(stringResource(R.string.current_status_format, label))
                        },
                        leadingContent = { Icon(Icons.Default.MusicVideo, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setQuitConfirmationEnabled(!quitConfirmationEnabled) },
                        headlineContent = { Text(stringResource(R.string.settings_quit_confirmation)) },
                        supportingContent = { Text(stringResource(R.string.settings_quit_confirmation_desc)) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = quitConfirmationEnabled, onCheckedChange = null)
                        }
                    )
                }

                item { SettingsSectionHeader(stringResource(R.string.settings_section_playback)) }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setAutoReconnectEnabled(!autoReconnectEnabled) },
                        headlineContent = { Text(stringResource(R.string.settings_auto_reconnect)) },
                        supportingContent = { Text(stringResource(R.string.settings_auto_reconnect_desc)) },
                        leadingContent = { Icon(Icons.Default.Autorenew, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = autoReconnectEnabled, onCheckedChange = null)
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setExtraBufferingEnabled(!extraBufferingEnabled) },
                        headlineContent = { Text(stringResource(R.string.settings_extra_buffering)) },
                        supportingContent = { Text(stringResource(R.string.settings_extra_buffering_desc)) },
                        leadingContent = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = extraBufferingEnabled, onCheckedChange = null)
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleAudioPassthrough() },
                        headlineContent = { Text(stringResource(R.string.settings_audio_passthrough)) },
                        supportingContent = { Text(stringResource(R.string.settings_audio_passthrough_desc)) },
                        leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = audioPassthrough, onCheckedChange = null)
                        }
                    )
                }

                item { SettingsSectionHeader(stringResource(R.string.settings_section_database)) }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleHideBroken() },
                        headlineContent = { Text(stringResource(R.string.settings_smart_filter)) },
                        supportingContent = { Text(stringResource(R.string.settings_smart_filter_desc)) },
                        leadingContent = { Icon(Icons.Default.FilterAlt, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = hideBroken, onCheckedChange = null)
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleMinTagFilter() },
                        headlineContent = { Text(stringResource(R.string.settings_min_tag_count)) },
                        supportingContent = { Text(stringResource(R.string.settings_min_tag_count_desc)) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = minTagFilter, onCheckedChange = null)
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("AutoUpdate") },
                        headlineContent = { Text(stringResource(R.string.settings_bg_sync)) },
                        supportingContent = {
                            val label = when(autoUpdateInterval) {
                                12 -> stringResource(R.string.settings_bg_sync_desc_12)
                                24 -> stringResource(R.string.settings_bg_sync_desc_24)
                                else -> stringResource(R.string.settings_bg_sync_desc_off)
                            }
                            Text(stringResource(R.string.update_frequency_format, label))
                        },
                        leadingContent = { Icon(Icons.Default.Sync, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.updateDatabase() },
                        headlineContent = { Text(stringResource(R.string.settings_force_refresh)) },
                        supportingContent = {
                            val dateStr = if (lastUpdate > 0) {
                                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastUpdate))
                            } else stringResource(R.string.last_synced_never)
                            Text(stringResource(R.string.last_synced_format, dateStr, serverStats?.stations?.toString() ?: "..."))
                        },
                        leadingContent = { Icon(Icons.Default.Radio, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                }

                item { SettingsSectionHeader(stringResource(R.string.data_management_header)) }

                item {
                    ListItem(
                        selected = false,
                        onClick = {
                            viewModel.openFilePicker(isExport = true, suggestedFileName = viewModel.getTimestampedBackupFileName())
                        },
                        headlineContent = { Text(stringResource(R.string.settings_backup_favs)) },
                        supportingContent = { Text(stringResource(R.string.settings_backup_favs_desc)) },
                        leadingContent = { Icon(Icons.Default.CloudUpload, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = {
                            viewModel.openFilePicker(isExport = false)
                        },
                        headlineContent = { Text(stringResource(R.string.settings_restore_favs)) },
                        supportingContent = { Text(stringResource(R.string.settings_restore_favs_desc)) },
                        leadingContent = { Icon(Icons.Default.CloudDownload, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = onPermissionRequest,
                        headlineContent = { Text(stringResource(R.string.settings_grant_storage)) },
                        supportingContent = { Text(stringResource(R.string.settings_grant_storage_desc)) },
                        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            colors = SurfaceDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.tv.material3.Border(
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                shape = MaterialTheme.shapes.medium
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp), 
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Radio, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(56.dp),
                                    tint = readableAccent()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "PURE RADIO TV", 
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = readableAccent()
                                )
                                Text(
                                    "A Premium Retro Experience", 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Build: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(BuildConfig.BUILD_TIME))} • v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "https://github.com/antoxa78/PureRadio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = readableAccent().copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: MainViewModel, 
    onLongClick: (Station) -> Unit = {}, 
    onTagGroupLongClick: ((String) -> Unit)? = null,
    isGenreDialogOpen: Boolean = false
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val tagSearchGroups by viewModel.tagSearchGroups.collectAsState()
    val selectedSearchTag by viewModel.selectedSearchTag.collectAsState()
    val lastBrowsedCategory by viewModel.lastBrowsedCategory.collectAsState()
    val visibleGenres by viewModel.visibleGenres.collectAsState()
    val selectedBitrates by viewModel.selectedBitrates.collectAsState()
    val hasMoreStations by viewModel.hasMoreStations.collectAsState()
    val searchFocusTrigger by viewModel.searchFocusTrigger.collectAsState()
    val searchFieldFocusRequester = remember { FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val searchTriggered = remember { mutableIntStateOf(0) }
    var localSearchQuery by remember { mutableStateOf(searchQuery) }
    var isSearchFieldFocused by remember { mutableStateOf(false) }

    val prevSelectedSearchTag = remember { mutableStateOf(selectedSearchTag) }
    val isReturning = selectedSearchTag == null && prevSelectedSearchTag.value != null
    LaunchedEffect(selectedSearchTag) {
        prevSelectedSearchTag.value = selectedSearchTag
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery != localSearchQuery) {
            localSearchQuery = searchQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 32.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it); localSearchQuery = it },
                label = { Text(stringResource(R.string.search_stations_hint)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
                    .focusRequester(searchFieldFocusRequester)
                    .onFocusChanged { isSearchFieldFocused = it.isFocused },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchTriggered.intValue++
                        viewModel.addToRecentSearches(localSearchQuery)
                        keyboardController?.hide()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = readableAccent(),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Button(
                onClick = { viewModel.toggleSearchMode() },
                modifier = Modifier.padding(bottom = 16.dp),
                colors = if (searchMode == com.toxa.pureradio.ui.viewmodel.SearchMode.Tag) {
                    androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    androidx.tv.material3.ButtonDefaults.colors()
                }
            ) {
                val isTagMode = searchMode == com.toxa.pureradio.ui.viewmodel.SearchMode.Tag
                Icon(
                    if (isTagMode) Icons.Default.Category else Icons.Default.Radio,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(if (isTagMode) R.string.search_mode_tag else R.string.search_mode_name))
            }
        }
        
        if (isLoading) {
            Text(stringResource(R.string.search_loading), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
        }

        BitrateFilters(
            selectedBitrates = selectedBitrates,
            onToggleFilter = { viewModel.toggleBitrateFilter(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.weight(1f)) {
            if (stations.isEmpty() && localSearchQuery.isEmpty() && tagSearchGroups.isEmpty()) {
                if (recentSearches.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.search_recent),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { viewModel.clearRecentSearches() },
                                colors = androidx.tv.material3.ButtonDefaults.colors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.search_clear_recent), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        LazyRow {
                            items(recentSearches) { query ->
                                Button(
                                    onClick = { 
                                        searchTriggered.intValue++
                                        viewModel.onSearchQueryChange(query) 
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(query)
                                }
                            }
                        }
                    }
                } else {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.search_empty_hint),
                        message = stringResource(R.string.search_empty_message)
                    )
                }
            } else if (searchMode == com.toxa.pureradio.ui.viewmodel.SearchMode.Tag && tagSearchGroups.isNotEmpty() && selectedSearchTag == null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.search_tag_filter_hint, stations.size),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        GenreGroupGrid(
                            groups = tagSearchGroups,
                            autoFocus = isReturning,
                            targetCategoryName = lastBrowsedCategory,
                            homeGenres = visibleGenres,
                            onGroupClick = { tagName ->
                                if (!isGenreDialogOpen) {
                                    viewModel.selectSearchTag(tagName)
                                }
                            },
                            onGroupLongClick = { tagName ->
                                onTagGroupLongClick?.invoke(tagName)
                            }
                        )
                    }
                }
            } else {

                Column(modifier = Modifier.fillMaxSize()) {
                    if (selectedSearchTag != null) {
                        Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedSearchTag!!.toTitleCase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.stations_count, stations.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StationGrid(
                            stations = stations, 
                            viewModel = viewModel, 
                            autoFocus = selectedSearchTag != null, 
                            isLongClickActive = isGenreDialogOpen,
                            onLongClick = onLongClick,
                            onLoadMore = if (hasMoreStations) { { viewModel.loadMoreStations() } } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreGroupGrid(
    groups: List<GenreGroup>,
    autoFocus: Boolean = true,
    targetCategoryName: String? = null,
    homeGenres: Set<String> = emptySet(),
    onGroupClick: (String) -> Unit,
    onGroupLongClick: ((String) -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()
    val targetIndex = remember(groups, targetCategoryName) {
        if (!targetCategoryName.isNullOrEmpty()) {
            val idx = groups.indexOfFirst { it.genreName.equals(targetCategoryName, ignoreCase = true) }
            if (idx >= 0) idx else 0
        } else 0
    }

    LaunchedEffect(autoFocus, groups.isNotEmpty(), targetIndex) {
        if (autoFocus && groups.isNotEmpty()) {
            if (targetIndex > 0) {
                try { gridState.scrollToItem(targetIndex) } catch (_: Exception) {}
                yield()
            }
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }
    if (groups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 140.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(groups, key = { _, group -> group.genreName }) { index, group ->
                GenreGroupCard(
                    group = group.copy(genreName = group.genreName.toTitleCase()),
                    onHome = group.genreName in homeGenres,
                    onClick = { onGroupClick(group.genreName) },
                    onLongClick = if (onGroupLongClick != null) { { onGroupLongClick(group.genreName) } } else null,
                    modifier = if (index == targetIndex) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GenreGroupCard(group: GenreGroup, onHome: Boolean = false, onClick: () -> Unit, onLongClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .padding(8.dp)
            .height(180.dp),
        scale = androidx.tv.material3.CardDefaults.scale(focusedScale = 1.1f),
        glow = androidx.tv.material3.CardDefaults.glow(
            focusedGlow = androidx.tv.material3.Glow(
                elevationColor = getGenreColor(group.genreName).copy(alpha = 0.5f),
                elevation = 12.dp
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(getGenreColor(group.genreName))) {
            AsyncImage(
                model = getGenreImageUrl(group.genreName),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            if (onHome) {
                HomeBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title uses the space left over after the count chip, so a long name that
                // wraps to more rows shrinks the title area instead of pushing the station
                // count chip out of the tile (which got clipped at the card edge).
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    GenreTitleText(
                        text = group.genreName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val countText = "${group.filteredCount} / ${group.totalStations}"
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Small "on Home tab" badge shown on genre tiles that have already been added to the
 * Home screen, so their state is visible at a glance while browsing.
 */
@Composable
fun HomeBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary),
        border = androidx.tv.material3.Border(
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)),
            shape = CircleShape
        )
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = stringResource(R.string.genre_on_home),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(6.dp).size(20.dp)
        )
    }
}

/**
 * "Added to Favourites" badge shown on station tiles so favourite stations are visible
 * at a glance while browsing. Hidden on the Favourites tab itself, where every tile is
 * already a favourite.
 */
@Composable
fun FavoriteBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        colors = SurfaceDefaults.colors(containerColor = Color(0xFFE53935)),
        border = androidx.tv.material3.Border(
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.7f)),
            shape = CircleShape
        )
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = stringResource(R.string.station_in_favorites),
            tint = Color.White,
            modifier = Modifier.padding(6.dp).size(20.dp)
        )
    }
}

/**
 * Category/tile title that always wraps whole words onto the next line and never breaks a
 * word mid-word ("divide letters"). The layout engine only splits a word when that word is
 * wider than the whole line, so the font is capped at a size that guarantees the longest
 * word fits on a single line — long names then simply use more rows of complete words.
 */
@Composable
fun GenreTitleText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    maxLines: Int = 3
) {
    BoxWithConstraints(modifier = modifier) {
        val longestWord = text.split(Regex("\\s+")).maxOfOrNull { it.length } ?: text.length
        // Rough estimate of the largest font at which the longest word still fits the
        // available width (bold text ≈ 0.72 * fontSize per character on average, slightly
        // over-estimated so the engine never has to break a word).
        val maxFontForFit = if (longestWord > 0) {
            maxWidth.value / (0.72f * (longestWord + 1f))
        } else {
            style.fontSize.value
        }
        val fontSize = minOf(style.fontSize.value, maxFontForFit)
            .coerceIn(12f, style.fontSize.value)
        Text(
            text = text,
            style = style,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = Color.White,
            softWrap = true,
            maxLines = maxLines,
            overflow = TextOverflow.Clip,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

fun getGenreColor(genre: String): Color {
    val hash = genre.hashCode()
    val r = (Math.abs(hash) % 100) + 20
    val g = (Math.abs(hash shr 8) % 100) + 20
    val b = (Math.abs(hash shr 16) % 100) + 20
    return Color(r, g, b)
}

fun getGenreImageUrl(genre: String): String {
    // Every URL below is verified to serve a real JPEG (HTTP GET returns 200 + image bytes).
    val g = genre.lowercase().trim()
    val pick = (g.hashCode() and Int.MAX_VALUE)
    val urls: List<String> = when {
        g.contains("heavy metal") || g.contains("металл") -> listOf(
            "https://images.unsplash.com/photo-1598387181032-a3103a2db5b3?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("metal") || g.contains("метал") -> listOf(
            "https://images.unsplash.com/photo-1598387181032-a3103a2db5b3?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("punk") || g.contains("hardcore") -> listOf(
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("christmas") || g.contains("xmas") || g.contains("новогодн") || g.contains("рождественск") -> listOf(
            "https://images.unsplash.com/photo-1543589077-47d81606c1bf?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1482517967863-00e15c9b44be?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("kids") || g.contains("children") || g.contains("детск") || g.contains("сказк") -> listOf(
            "https://images.unsplash.com/photo-1516627145497-ae6968895b74?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("sport") || g.contains("спорт") || g.contains("футбол") -> listOf(
            "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("news") || g.contains("talk") || g.contains("info") || g.contains("новост") ||
        g.contains("разговорн") -> listOf(
            "https://images.unsplash.com/photo-1472289065668-ce650ac443d2?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1571330735066-03aaa9429d89?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("hard rock") || g.contains("хард-рок") -> listOf(
            "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("classic rock") || g.contains("классик-рок") -> listOf(
            "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("rock") || g.contains("рок") -> listOf(
            "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("alternative") || g.contains("indie") || g.contains("альтернатив") -> listOf(
            "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("smooth jazz") -> listOf(
            "https://images.unsplash.com/photo-1524678606370-a47ad25cb82a?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("jazz") || g.contains("swing") || g.contains("big band") || g.contains("джаз") -> listOf(
            "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1558584673-c834fb1cc3ca?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("blues") || g.contains("блюз") -> listOf(
            "https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1468164016595-6108e4c60c8b?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("soul") || g.contains("r&b") || g.contains("rhythm and blues") || g.contains("соул") -> listOf(
            "https://images.unsplash.com/photo-1460723237483-7a6dc9d0b212?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("hip hop") || g.contains("hip-hop") || g.contains("rap") || g.contains("urban") ||
        g.contains("хип-хоп") || g.contains("рэп") -> listOf(
            "https://images.unsplash.com/photo-1520262454473-a1a82276a574?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("synthpop") || g.contains("synthwave") || g.contains("retrowave") || g.contains("синтипоп") -> listOf(
            "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("disco") || g.contains("funk") || g.contains("groove") || g.contains("диско") || g.contains("фанк") -> listOf(
            "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("latin") || g.contains("salsa") || g.contains("bachata") || g.contains("cumbia") ||
        g.contains("merengue") || g.contains("reggaeton") || g.contains("латино") -> listOf(
            "https://images.unsplash.com/photo-1508138221679-760a23a2285b?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("country") || g.contains("western") || g.contains("cowboy") || g.contains("кантри") -> listOf(
            "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517260739337-6799d239ce83?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("bluegrass") -> listOf(
            "https://images.unsplash.com/photo-1468164016595-6108e4c60c8b?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("folk") || g.contains("acoustic") || g.contains("singer-songwriter") || g.contains("фолк") ||
        g.contains("народн") || g.contains("шансон") -> listOf(
            "https://images.unsplash.com/photo-1468164016595-6108e4c60c8b?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("reggae") || g.contains("ska") || g.contains("dub") || g.contains("dancehall") ||
        g.contains("регги") || g.contains("ска") -> listOf(
            "https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("classical") || g.contains("chamber") || g.contains("baroque") || g.contains("piano") ||
        g.contains("violin") || g.contains("классическ") || g.contains("камерн") || g.contains("инструменталь") -> listOf(
            "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("orchestra") || g.contains("symphony") || g.contains("philharmonic") ||
        g.contains("оркестр") || g.contains("симфон") -> listOf(
            "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("opera") || g.contains("оперн") -> listOf(
            "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("techno") || g.contains("техно") -> listOf(
            "https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("deep house") -> listOf(
            "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("house") || g.contains("хаус") -> listOf(
            "https://images.unsplash.com/photo-1557683316-973673baf926?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("trance") || g.contains("транс") -> listOf(
            "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("psytrance") || g.contains("goa") -> listOf(
            "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("dance") || g.contains("данс") -> listOf(
            "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("drum and bass") || g.contains("dnb") || g.contains("dubstep") ||
        g.contains("electro") || g.contains("electronica") || g.contains("electronic") ||
        g.contains("электро") || g.contains("танцевальн") -> listOf(
            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("dj") || g.contains("mix") || g.contains("диджей") -> listOf(
            "https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("ambient") || g.contains("new age") || g.contains("эмбиент") || g.contains("нью-эйдж") -> listOf(
            "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("chillout") || g.contains("chill") || g.contains("lounge") ||
        g.contains("easy listening") || g.contains("lofi") || g.contains("lo-fi") ||
        g.contains("чиллаут") || g.contains("лаунж") -> listOf(
            "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("meditation") || g.contains("spiritual") || g.contains("religious") ||
        g.contains("yoga") || g.contains("медитац") -> listOf(
            "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("soundtrack") || g.contains("movie") || g.contains("film") ||
        g.contains("саундтрек") || g.contains("кино") -> listOf(
            "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("comedy") || g.contains("юмор") -> listOf(
            "https://images.unsplash.com/photo-1527224857830-43a7acc85260?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("world") || g.contains("global") || g.contains("international") ||
        g.contains("traditional") || g.contains("миров") -> listOf(
            "https://images.unsplash.com/photo-1526218626217-dc65a29bb444?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1489392191049-fc10c97e64b6?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("80s") || g.contains("80's") || g.contains("1980") || g.contains("80er") || g.contains("80х") -> listOf(
            "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("90s") || g.contains("90's") || g.contains("1990") || g.contains("90х") -> listOf(
            "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511379938547-c1f69419868d?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("70s") || g.contains("70's") || g.contains("1970") || g.contains("70х") -> listOf(
            "https://images.unsplash.com/photo-1516062423079-7ca13cdc7f5a?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("60s") || g.contains("60's") || g.contains("50s") || g.contains("oldies") ||
        g.contains("retro") || g.contains("nostalg") || g.contains("ретро") || g.contains("ностальг") -> listOf(
            "https://images.unsplash.com/photo-1511379938547-c1f69419868d?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=800&auto=format&fit=crop"
        )
        g.contains("pop") || g.contains("hits") || g.contains("top") || g.contains("chart") ||
        g.contains("music") || g.contains("эстрад") || g.contains("популярн") -> listOf(
            "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
        else -> listOf(
            "https://images.unsplash.com/photo-1453090927415-5f45085b65c0?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop"
        )
    }
    return urls[pick % urls.size]
}

@Composable
fun StationGrid(
    stations: List<Station>, 
    viewModel: MainViewModel, 
    autoFocus: Boolean = true,
    isLongClickActive: Boolean = false,
    onLoadMore: (() -> Unit)? = null,
    onLongClick: (Station) -> Unit = {}
) {
    val currentStation by viewModel.currentStation.collectAsState()
    val selectedNavItem by viewModel.selectedNavItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val loadMoreFocusRequester = remember { FocusRequester() }
    var loadMoreCount by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(autoFocus, stations.isNotEmpty()) {
        if (autoFocus && loadMoreCount == 0) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading && loadMoreCount > 0) {
            delay(100)
            try { loadMoreFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    // Only show the "nothing here" message once the list has stayed empty for a moment
    // without loading, so it doesn't flash between a search keystroke and its request.
    var showEmptyState by remember { mutableStateOf(false) }
    LaunchedEffect(stations.isEmpty(), isLoading) {
        showEmptyState = false
        if (stations.isEmpty() && !isLoading) {
            delay(600)
            showEmptyState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
    ) {
        if (stations.isEmpty()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = readableAccent()
                    )
                }
            } else if (showEmptyState) {
                when (selectedNavItem) {
                    NavigationItem.Favourites -> EmptyState(
                        icon = Icons.Default.FavoriteBorder,
                        title = stringResource(R.string.empty_favorites_title),
                        message = stringResource(R.string.empty_favorites_message)
                    )
                    NavigationItem.Recent -> EmptyState(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.empty_recent_title),
                        message = stringResource(R.string.empty_recent_message)
                    )
                    else -> EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = stringResource(R.string.empty_no_stations_title),
                        message = stringResource(R.string.empty_no_stations_message)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 140.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(stations, key = { _, station -> station.stationUuid }) { index, station ->
                    StationCard(
                        station = station,
                        isFavorite = selectedNavItem != NavigationItem.Favourites && viewModel.isFavorite(station),
                        isCurrent = currentStation?.stationUuid == station.stationUuid,
                        onClick = { if (!isLongClickActive) viewModel.playStation(station) },
                        onLongClick = { onLongClick(station) },
                        modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                    )
                }
                
                if (onLoadMore != null) {
                    item(key = "load_more", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(5) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    if (!isLoading) {
                                        loadMoreCount++
                                        onLoadMore()
                                    }
                                },
                                modifier = Modifier.focusRequester(loadMoreFocusRequester),
                                colors = androidx.tv.material3.ButtonDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    contentColor = readableAccent()
                                )
                            ) {
                                if (isLoading) {
                                    Text(stringResource(R.string.loading_ellipsis), style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.load_more_stations), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagGrid(
    tags: List<Tag>,
    autoFocus: Boolean = true,
    targetTagName: String? = null,
    homeGenres: Set<String> = emptySet(),
    onTagClick: (Tag) -> Unit,
    onTagLongClick: ((Tag) -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()
    val targetIndex = remember(tags, targetTagName) {
        if (!targetTagName.isNullOrEmpty()) {
            val idx = tags.indexOfFirst { it.name.equals(targetTagName, ignoreCase = true) }
            if (idx >= 0) idx else 0
        } else 0
    }

    LaunchedEffect(autoFocus, tags.isNotEmpty(), targetIndex) {
        if (autoFocus && tags.isNotEmpty()) {
            if (targetIndex > 0) {
                try { gridState.scrollToItem(targetIndex) } catch (_: Exception) {}
                yield()
            }
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (tags.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize())
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 140.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(tags, key = { _, tag -> tag.name }) { index, tag ->
                Card(
                    onClick = { onTagClick(tag) },
                    onLongClick = if (onTagLongClick != null) { { onTagLongClick(tag) } } else null,
                    modifier = Modifier
                        .padding(8.dp)
                        .height(180.dp)
                        .then(if (index == targetIndex) Modifier.focusRequester(focusRequester) else Modifier),
                    scale = androidx.tv.material3.CardDefaults.scale(focusedScale = 1.1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(getGenreColor(tag.name))) {
                        AsyncImage(
                            model = getGenreImageUrl(tag.name),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.4f
                        )
                        Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                        if (homeGenres.contains(tag.name)) {
                            HomeBadge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GenreTitleText(
                                text = tag.name.toTitleCase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(R.string.count_stations_plain, tag.stationcount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CountryGrid(
    countries: List<Country>, 
    autoFocus: Boolean = true, 
    targetCountryName: String? = null,
    onCountryClick: (Country) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()
    val targetIndex = remember(countries, targetCountryName) {
        if (!targetCountryName.isNullOrEmpty()) {
            val idx = countries.indexOfFirst { 
                it.name.equals(targetCountryName, ignoreCase = true) || 
                it.iso_3166_1.equals(targetCountryName, ignoreCase = true) 
            }
            if (idx >= 0) idx else 0
        } else 0
    }

    LaunchedEffect(autoFocus, countries.isNotEmpty(), targetIndex) {
        if (autoFocus && countries.isNotEmpty()) {
            if (targetIndex > 0) {
                try { gridState.scrollToItem(targetIndex) } catch (_: Exception) {}
                yield()
            }
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (countries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 140.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(countries, key = { _, country -> country.iso_3166_1 }) { index, country ->
                Card(
                    onClick = { onCountryClick(country) },
                    modifier = Modifier
                        .padding(8.dp)
                        .height(180.dp)
                        .then(if (index == targetIndex) Modifier.focusRequester(focusRequester) else Modifier),
                    scale = androidx.tv.material3.CardDefaults.scale(focusedScale = 1.1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(getGenreColor(country.name))) {
                        val flagCode = country.iso_3166_1.lowercase().trim()
                        AsyncImage(
                            model = if (flagCode.isNotEmpty()) "https://flagcdn.com/w320/$flagCode.png" else "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?q=80&w=600&auto=format&fit=crop",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.35f,
                            error = coil.compose.rememberAsyncImagePainter("https://images.unsplash.com/photo-1526772662000-3f88f10405ff?q=80&w=600&auto=format&fit=crop")
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GenreTitleText(
                                text = country.name.toTitleCase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = stringResource(R.string.count_stations_plain, country.stationcount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StationCard(
    station: Station,
    isFavorite: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val accent = readableAccent()
    val focusedBorder = androidx.tv.material3.Border(
        border = androidx.compose.foundation.BorderStroke(2.dp, accent),
        shape = MaterialTheme.shapes.medium
    )
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .padding(8.dp)
            .height(180.dp),
        scale = androidx.tv.material3.CardDefaults.scale(focusedScale = 1.1f),
        glow = androidx.tv.material3.CardDefaults.glow(
            focusedGlow = androidx.tv.material3.Glow(
                elevationColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                elevation = 12.dp
            )
        ),
        // The station that is playing keeps a soft outline even when not focused, so it
        // is easy to spot while scrolling through a long grid.
        border = androidx.tv.material3.CardDefaults.border(
            border = if (isCurrent) {
                androidx.tv.material3.Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, accent.copy(alpha = 0.55f)),
                    shape = MaterialTheme.shapes.medium
                )
            } else {
                androidx.tv.material3.Border.None
            },
            focusedBorder = focusedBorder,
            pressedBorder = focusedBorder
        ),
        colors = androidx.tv.material3.CardDefaults.colors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(90.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    AsyncImage(
                        model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit,
                        error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo),
                        placeholder = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
                    )
                }
                val code = station.countryCode?.trim()?.lowercase()
                if (!code.isNullOrEmpty() && code.length == 2) {
                    AsyncImage(
                        model = "https://flagcdn.com/w80/$code.png",
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).align(Alignment.TopStart).padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                if (isFavorite) {
                    FavoriteBadge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
                if (isCurrent) {
                    // "Now playing" pill, large enough to read from the couch.
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 6.dp),
                        shape = CircleShape,
                        colors = SurfaceDefaults.colors(containerColor = accent)
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            modifier = Modifier.padding(4.dp).size(18.dp),
                            tint = if (accent.luminance() > 0.4f) Color.Black else Color.White
                        )
                    }
                }
            }
            
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) accent else Color.Unspecified,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = mutedColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = station.votes.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedColor
                    )
                }
                // Codec + bitrate at a glance, e.g. "MP3 · 128k" / "FLAC".
                stationQualityLabel(station)?.let { quality ->
                    val isLossless = station.codec.orEmpty().contains("FLAC", ignoreCase = true)
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = SurfaceDefaults.colors(
                            containerColor = if (isLossless) accent.copy(alpha = 0.25f)
                                             else Color.White.copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            text = quality,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isLossless) FontWeight.Bold else FontWeight.Medium,
                            color = if (isLossless) accent else mutedColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class)
@Composable
fun Screensaver(viewModel: MainViewModel) {
    val currentStation by viewModel.currentStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackTime by viewModel.playbackTime.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val audioFormat by viewModel.audioFormat.collectAsState()
    val mediaMetadata by viewModel.mediaMetadata.collectAsState()
    val focusRequester = remember { FocusRequester() }

    val infiniteTransition = rememberInfiniteTransition(label = "BouncingTransition")
    
    val xOffset by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "xOffset"
    )
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent {
                viewModel.resetScreensaverTimer()
                true
            },
        colors = SurfaceDefaults.colors(containerColor = Color.Black)
    ) {
        if (screensaverMode == com.toxa.pureradio.ui.viewmodel.ScreensaverMode.StationInfo) {
            Box(
                modifier = Modifier.fillMaxSize(), 
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.Center)
                        .offset(
                            x = (xOffset * 400).dp,
                            y = (yOffset * 200).dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    currentStation?.let { station ->
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                                contentDescription = null,
                                modifier = Modifier.size(260.dp),
                                contentScale = ContentScale.Fit,
                                error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo),
                                placeholder = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
                            )
                            
                            val code = station.countryCode?.trim()?.lowercase()
                            if (!code.isNullOrEmpty() && code.length == 2) {
                                AsyncImage(
                                    model = "https://flagcdn.com/w160/$code.png",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.TopStart)
                                        .offset(x = (-20).dp, y = (-10).dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Technical info
                        val technicalInfo = formatTechnicalInfo(audioFormat, station)

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = technicalInfo,
                                style = MaterialTheme.typography.labelLarge,
                                color = readableAccent(),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        val displayTitle = if (!mediaMetadata?.title.isNullOrEmpty()) {
                            mediaMetadata?.title.toString()
                        } else {
                            station.name
                        }
                        
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (!mediaMetadata?.artist.isNullOrEmpty()) {
                            Text(
                                text = mediaMetadata?.artist.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        val timeStr = formatElapsed(playbackTime)
                        
                        Text(
                            text = stringResource(
                                if (isPlaying) R.string.playback_status_playing else R.string.playback_status_paused,
                                timeStr
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            color = readableAccent().copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        WaveformAnalyzer(isPlaying = isPlaying)

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.screensaver_return_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilePicker(
    state: MainViewModel.FilePickerState,
    onNavigate: (java.io.File) -> Unit,
    onNavigateUp: () -> Unit,
    onSelected: (java.io.File) -> Unit,
    onDismiss: () -> Unit
) {
    val folderIconColor = Color(0xFFFFCA28)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                shape = MaterialTheme.shapes.large
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        if (state.isExport) Icons.Default.CreateNewFolder else Icons.Default.Folder,
                        contentDescription = null,
                        tint = readableAccent(),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (state.isExport) stringResource(R.string.file_picker_backup_title) else stringResource(R.string.settings_restore_favs),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.action_close))
                    }
                }
                
                Surface(
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp), tint = folderIconColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.currentPath, 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (state.currentPath != state.rootPath) {
                        item {
                            ListItem(
                                selected = false,
                                onClick = onNavigateUp,
                                headlineContent = { Text("..", fontWeight = FontWeight.Bold) },
                                supportingContent = { Text(stringResource(R.string.go_to_parent_directory)) },
                                leadingContent = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = folderIconColor) }
                            )
                        }
                    }
                    
                    items(state.files) { file ->
                        val isPlaylist = file.name.lowercase().let { 
                            it.endsWith(".m3u") || it.endsWith(".m3u8") || it.endsWith(".pls") || it.endsWith(".txt")
                        }
                        
                        ListItem(
                            selected = false,
                            onClick = {
                                if (file.isDirectory) {
                                    onNavigate(file)
                                } else {
                                    onSelected(file)
                                }
                            },
                            enabled = true,
                            headlineContent = { 
                                Text(
                                    file.name,
                                    fontWeight = if (file.isDirectory) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            leadingContent = {
                                Icon(
                                    if (file.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (file.isDirectory) folderIconColor 
                                           else if (isPlaylist) MaterialTheme.colorScheme.primary
                                           else Color.Gray
                                )
                            },
                            supportingContent = {
                                if (file.isDirectory) {
                                    Text(stringResource(R.string.file_type_folder), style = MaterialTheme.typography.labelSmall)
                                } else {
                                    val size = file.length()
                                    val sizeStr = if (size > 1024 * 1024) "${size / (1024 * 1024)} MB" else "${size / 1024} KB"
                                    Text(sizeStr, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }
                
                if (state.isExport) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.export_filename_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = readableAccent()
                                )
                                Text(
                                    text = state.suggestedFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { onSelected(java.io.File(state.currentPath)) },
                                colors = androidx.tv.material3.ButtonDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(stringResource(R.string.action_save_here))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class)
@Composable
fun NowPlayingBar(
    station: Station,
    isPlaying: Boolean,
    isFavorite: Boolean,
    playbackTime: Long,
    playbackDuration: Long,
    mediaMetadata: androidx.media3.common.MediaMetadata?,
    audioFormat: androidx.media3.common.Format?,
    onTogglePlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val accent = readableAccent()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        shape = RectangleShape
    ) {
        // Thin accent line along the top edge separates the bar from the grid scrolling under it.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color.Transparent, accent.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left part: Station info, bitrate, flag
            Row(
                modifier = Modifier.weight(1.2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(60.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    AsyncImage(
                        model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit,
                        error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Technical decoder info
                    val technicalInfo = formatTechnicalInfo(audioFormat, station)

                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Live streams have no duration; mark them with a small "LIVE" tag.
                        if (isPlaying && playbackDuration <= 0) {
                            LiveBadge()
                        }
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = technicalInfo,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val displayTitle = if (!mediaMetadata?.title.isNullOrEmpty()) {
                        mediaMetadata?.title.toString()
                    } else {
                        station.name
                    }
                    val displaySubtitle = if (!mediaMetadata?.artist.isNullOrEmpty()) {
                        mediaMetadata?.artist.toString()
                    } else {
                        station.country
                    }

                    Text(
                        text = displayTitle, 
                        style = MaterialTheme.typography.titleLarge, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mediaMetadata?.artist.isNullOrEmpty()) {
                            val code = station.countryCode?.trim()?.lowercase()
                            if (!code.isNullOrEmpty() && code.length == 2) {
                                AsyncImage(
                                    model = "https://flagcdn.com/w80/$code.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Text(
                            text = displaySubtitle, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (!mediaMetadata?.title.isNullOrEmpty()) {
                            Text(
                                text = " • ${station.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Center part: Controls
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatElapsed(playbackTime),
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Card(onClick = onPrevious, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.content_desc_previous),
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                Card(
                    onClick = onTogglePlay, 
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = androidx.tv.material3.CardDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) R.string.content_desc_pause else R.string.content_desc_play),
                        modifier = Modifier.padding(12.dp).size(28.dp)
                    )
                }
                Card(onClick = onNext, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.content_desc_next),
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Card(
                    onClick = onToggleFavorite, 
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.content_desc_favorite),
                        modifier = Modifier.padding(10.dp).size(24.dp),
                        // Same red as the favourite badge on station tiles.
                        tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Right part: Waveform
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                WaveformAnalyzer(isPlaying = isPlaying)
            }
        }

        if (playbackDuration > 0) {
            LinearProgressIndicator(
                progress = { playbackTime.toFloat() / playbackDuration.toFloat() },
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter),
                color = accent,
                trackColor = Color.Transparent
            )
        }
    }
}

/** Small pulsing "LIVE" tag for live streams in the Now Playing bar. */
@Composable
fun LiveBadge() {
    val pulse by rememberInfiniteTransition(label = "LivePulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "LivePulseAlpha"
    )
    val liveRed = Color(0xFFE53935)
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        colors = SurfaceDefaults.colors(containerColor = liveRed.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer { alpha = pulse }
                    .background(liveRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.now_playing_live),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = liveRed
            )
        }
    }
}

@Composable
fun WaveformAnalyzer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val accent = readableAccent()
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")
    val barCount = 40
    
    Row(
        modifier = modifier
            .width(220.dp)
            .height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { i ->
            val distanceFromCenter = Math.abs(i - barCount / 2).toFloat()
            val centerWeight = 1f - (distanceFromCenter / (barCount / 2))
            
            val duration = remember { (500..1200).random() }
            val delay = remember { (i * 35) % 800 }
            
            val heightScale by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, delay, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "WaveHeight_$i"
            )
            
            val finalHeight = if (isPlaying) {
                (0.1f + 0.9f * heightScale) * (0.2f + 0.8f * centerWeight)
            } else {
                0.05f
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(finalHeight)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}
