package com.toxa.pureradio

import android.os.Bundle
import android.net.Uri
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.graphics.drawable.Icon
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.material.icons.filled.TheaterComedy
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.toxa.pureradio.BuildConfig
import androidx.compose.ui.unit.dp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val isInPipMode = mutableStateOf(false)

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, IntentFilter("ACTION_STOP_RADIO"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopReceiver, IntentFilter("ACTION_STOP_RADIO"))
        }
        setContent {
            val isPip by isInPipMode
            val isInitialized by viewModel.isInitialized.collectAsState()
            var splashElapsed by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(3000)
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
            val metadata = viewModel.mediaMetadata.value
            
            val stopIntent = PendingIntent.getBroadcast(
                this,
                1,
                Intent("ACTION_STOP_RADIO"),
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
            }
            
            // For Android 14+ we call enterPictureInPictureMode manually if not auto-triggered
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                 try {
                     enterPictureInPictureMode(params)
                 } catch (e: Exception) {}
            }
        }
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
                        contentDescription = "Open",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
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
                text = "Thousands of stations, for free",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color(0xFFB0BEC5),
                modifier = Modifier.graphicsLayer { alpha = subAlpha.value }
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
    val successMessage by viewModel.successMessage.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedSearchTag by viewModel.selectedSearchTag.collectAsState()
    val selectedBitrates by viewModel.selectedBitrates.collectAsState()
    val hasMoreStations by viewModel.hasMoreStations.collectAsState()
    val settingsSubMenu by viewModel.settingsSubMenu.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
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
            if (quitConfirmationEnabled) {
                showExitDialog = true
            } else {
                kotlin.system.exitProcess(0)
            }
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
                                    showExitDialog = true
                                } else {
                                    viewModel.selectNavigationItem(item)
                                    drawerState.setValue(DrawerValue.Closed)
                                }
                            },
                            colors = androidx.tv.material3.NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .focusProperties { canFocus = isDrawerCurrentlyOpen }
                                .then(if (isDrawerCurrentlyOpen) Modifier.focusRequester(drawerFocusRequesters[item]!!) else Modifier),
                            leadingContent = {
                                val icon = when (item) {
                                    NavigationItem.Home -> Icons.Default.Home
                                    NavigationItem.Popular -> Icons.Default.Mic
                                    NavigationItem.Recent -> Icons.Default.History
                                    NavigationItem.Search -> Icons.Default.Search
                                    NavigationItem.Genres -> Icons.AutoMirrored.Filled.List
                                    NavigationItem.Countries -> Icons.Default.Place
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
                            val label = when (item) {
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
                    val title = when {
                    selectedTag != null -> {
                        val name = selectedTag!!.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        val stationsText = stringResource(R.string.stations_count, stations.size)
                        "$name $stationsText"
                    }
                    selectedCountry != null -> {
                        val name = selectedCountry!!.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        val stationsText = stringResource(R.string.stations_count, stations.size)
                        "$name $stationsText"
                    }
                    else -> when (selectedNavItem) {
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
                }
                val isDeepDive = selectedTag != null || selectedCountry != null
                
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
                                text = if (selectedTag != null) "GENRE" else "COUNTRY",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = if (isDeepDive) FontWeight.ExtraBold else FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

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
                    if (error == null && successMessage == null) {
                        when (selectedNavItem) {
                            NavigationItem.Home -> {
                                if (genreGroups.isNotEmpty()) {
                                    if (selectedTag == null && selectedCountry == null) {
                                        key("home_groups") {
                                            GenreGroupGrid(
                                                groups = genreGroups,
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
                                                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                                                containerColor = Color.Transparent,
                                                                contentColor = Color.White
                                                            )
                                                        ) {
                                                            Text("X", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                                                        }
                                                    }
                                                },
                                                colors = TextFieldDefaults.colors(
                                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent
                                                )
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text("Sort: ", style = MaterialTheme.typography.labelLarge)
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
                                                        Text(if (mode == com.toxa.pureradio.ui.viewmodel.GenreSortMode.Name) "Name" else "Count")
                                                    }
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TagGrid(
                                                    tags = filteredTags,
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
                                        CountryGrid(countries) { viewModel.selectCountry(it) }
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
                    }
                    
                    if (isLoading && selectedNavItem != NavigationItem.Search) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
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

            if (error != null || successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .focusable(false),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        error?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                        successMessage?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
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
                    isFavorite = favorites.contains(station.stationUuid),
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
                        "File \"${file.name}\" already exists. Overwrite it?",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { viewModel.cancelOverwrite() },
                            modifier = Modifier.weight(1f).focusRequester(overwriteFocusRequester)
                        ) {
                            Text("No", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = { viewModel.confirmOverwrite() },
                            modifier = Modifier.weight(1f),
                            colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Yes", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        val isAlreadyFavorite = favorites.contains(station.stationUuid)
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isAlreadyFavorite) "Remove from Collection?" else "Add to Collection?",
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
                            Text("CANCEL", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                                if (isAlreadyFavorite) "REMOVE" else "ADD",
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
                            tint = MaterialTheme.colorScheme.primary
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
                        text = tag.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
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
                            Text("BACK", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                                "ADD",
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
                            tint = MaterialTheme.colorScheme.primary
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
                        text = genreName.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
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
                            Text("BACK", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                                "REMOVE",
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
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Exit The Application?",
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
                            Text("NO", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Button(
                            onClick = { kotlin.system.exitProcess(0) },
                            modifier = Modifier.weight(1f).focusRequester(exitYesFocusRequester),
                            colors = androidx.tv.material3.ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("YES", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
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
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Restore Favourites", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Found ${stations.size} stations. Choose action:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 16.dp))
                        
                        Button(
                            onClick = { viewModel.confirmRestore(replace = false) },
                            modifier = Modifier.fillMaxWidth().focusRequester(restoreFocusRequester)
                        ) {
                            Text("Add to current favorites", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showConfirmation = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Replace all favorites", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.cancelRestore() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                        Text("Are you sure?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("This will delete all your current favorites and replace them with the ones from the file.", 
                            style = MaterialTheme.typography.bodyMedium, 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { showConfirmation = false },
                                modifier = Modifier.weight(1f).focusRequester(restoreFocusRequester)
                            ) {
                                Text("No", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                            Button(
                                onClick = { viewModel.confirmRestore(replace = true) },
                                modifier = Modifier.weight(1f),
                                colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Yes", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        Text("Filter Stations By Bitrate: ", style = MaterialTheme.typography.labelLarge)
        BitrateFilter.entries.forEach { filter ->
            val label = when (filter) {
                BitrateFilter.Low -> "Low (<192)"
                BitrateFilter.High -> "High (>=192)"
                BitrateFilter.FLAC -> "FLAC"
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
                
                item {
                    Text("GENRES", style = MaterialTheme.typography.titleMedium, 
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.primary)
                }
                
                items(filteredTags) { tag ->
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleGenreVisibility(tag.name) },
                        headlineContent = { 
                            Text(tag.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }) 
                        },
                        supportingContent = {
                            Text("${tag.stationcount} stations")
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_db_update_interval), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                val options = listOf(
                    0 to "Manual Only (Off)",
                    12 to "Every 12 Hours",
                    24 to "Every 24 Hours"
                )
                options.forEachIndexed { index, (hours, label) ->
                    item(key = hours) {
                        ListItem(
                            selected = autoUpdateInterval == hours,
                            onClick = { 
                                viewModel.setAutoUpdateInterval(hours)
                                viewModel.setSettingsSubMenu(null)
                            },
                            modifier = if (index == 0) Modifier.focusRequester(subMenuFocusRequester) else Modifier,
                            headlineContent = { Text(label) },
                            trailingContent = {
                                if (autoUpdateInterval == hours) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        headlineContent = { Text("Activate Screensaver") },
                        supportingContent = { Text("Automatically engage when music is playing") },
                        trailingContent = {
                            Switch(checked = screensaverEnabled, onCheckedChange = null)
                        }
                    )
                }
                item {
                    Text("Display Mode", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), color = MaterialTheme.colorScheme.primary)
                }
                val modes = listOf(
                    com.toxa.pureradio.ui.viewmodel.ScreensaverMode.StationInfo to "Retro Station Info",
                    com.toxa.pureradio.ui.viewmodel.ScreensaverMode.BlackScreen to "Deep Black (OLED Safe)"
                )
                items(modes) { (mode, label) ->
                    ListItem(
                        selected = screensaverMode == mode,
                        onClick = { viewModel.setScreensaverMode(mode) },
                        headlineContent = { Text(label) },
                        trailingContent = {
                            if (screensaverMode == mode) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
                item {
                    Text("Idle Timeout", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), color = MaterialTheme.colorScheme.primary)
                }
                val timeouts = listOf(1, 5, 10, 20, 30)
                items(timeouts) { minutes ->
                    ListItem(
                        selected = screensaverTimeout == minutes,
                        onClick = {
                            viewModel.setScreensaverTimeout(minutes)
                            viewModel.setSettingsSubMenu(null)
                        },
                        headlineContent = { Text("$minutes Minutes") },
                        trailingContent = {
                            if (screensaverTimeout == minutes) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_app_theme), style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(AppTheme.entries) { theme ->
                    val label = when (theme) {
                        AppTheme.ModernBlue -> "Modern Blue"
                        AppTheme.RetroGold -> "Retro Gold"
                        AppTheme.BlueNeon -> "Blue Neon"
                        AppTheme.Violet -> "Violet"
                        AppTheme.Monochrome -> "Monochrome"
                        AppTheme.Forest -> "Forest"
                        AppTheme.Contrast -> "Contrast"
                    }
                    val desc = when (theme) {
                        AppTheme.ModernBlue -> "Sleek blue with high contrast"
                        AppTheme.RetroGold -> "Classic gold and wood tones"
                        AppTheme.BlueNeon -> "Cool blue neon glow"
                        AppTheme.Violet -> "Purple and violet tones"
                        AppTheme.Monochrome -> "Grey and black"
                        AppTheme.Forest -> "Green and black"
                        AppTheme.Contrast -> "White and black"
                    }
                    ListItem(
                        selected = appTheme == theme,
                        onClick = { viewModel.setAppTheme(theme) },
                        headlineContent = { Text(label) },
                        supportingContent = { Text(desc) },
                        trailingContent = {
                            if (appTheme == theme) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        headlineContent = { Text(language.name) },
                        trailingContent = {
                            if (appLanguage == language) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        headlineContent = { Text(item.name) },
                        trailingContent = {
                            if (defaultCategory == item) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 170.dp)
            ) {
                item {
                    Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("AppTheme") },
                        modifier = Modifier.focusRequester(mainMenuFocusRequester),
                        headlineContent = { Text(stringResource(R.string.settings_app_theme)) },
                        supportingContent = {
                            val label = when (appTheme) {
                                AppTheme.ModernBlue -> "Modern Blue"
                                AppTheme.RetroGold -> "Retro Gold"
                                AppTheme.BlueNeon -> "Blue Neon"
                                AppTheme.Violet -> "Violet"
                                AppTheme.Monochrome -> "Monochrome"
                                AppTheme.Forest -> "Forest"
                                AppTheme.Contrast -> "Contrast"
                            }
                            Text("Current: $label")
                        },
                        leadingContent = { Icon(Icons.Default.TheaterComedy, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("AppLanguage") },
                        headlineContent = { Text(stringResource(R.string.settings_app_language)) },
                        supportingContent = { Text("Current: ${appLanguage.name}") },
                        leadingContent = { Icon(Icons.Default.Public, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("DefaultCategory") },
                        headlineContent = { Text(stringResource(R.string.settings_startup_category)) },
                        supportingContent = { Text("Currently: ${defaultCategory.name}") },
                        leadingContent = { Icon(Icons.Default.Home, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setQuitConfirmationEnabled(!quitConfirmationEnabled) },
                        headlineContent = { Text(stringResource(R.string.settings_quit_confirmation)) },
                        supportingContent = { Text(stringResource(R.string.settings_quit_confirmation_desc)) },
                        leadingContent = { Icon(Icons.Default.Warning, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = quitConfirmationEnabled, onCheckedChange = null)
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setAutoReconnectEnabled(!autoReconnectEnabled) },
                        headlineContent = { Text(stringResource(R.string.settings_auto_reconnect)) },
                        supportingContent = { Text(stringResource(R.string.settings_auto_reconnect_desc)) },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
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
                        onClick = { viewModel.setSettingsSubMenu("Screensaver") },
                        headlineContent = { Text(stringResource(R.string.settings_ambient_screensaver)) },
                        supportingContent = {
                            val label = if (screensaverEnabled) stringResource(R.string.settings_ambient_screensaver_desc_enabled, screensaverTimeout) else stringResource(R.string.settings_ambient_screensaver_desc_disabled)
                            Text("Current status: $label")
                        },
                        leadingContent = { Icon(Icons.Default.MusicVideo, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleHideBroken() },
                        headlineContent = { Text(stringResource(R.string.settings_smart_filter)) },
                        supportingContent = { Text(stringResource(R.string.settings_smart_filter_desc)) },
                        leadingContent = { Icon(Icons.Default.Public, contentDescription = null) },
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
                            Text("Update frequency: $label") 
                        },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    Text("DATA MANAGEMENT", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), color = MaterialTheme.colorScheme.primary)
                    
                    ListItem(
                        selected = false,
                        onClick = { 
                            viewModel.openFilePicker(isExport = true, suggestedFileName = viewModel.getTimestampedBackupFileName())
                        },
                        headlineContent = { Text(stringResource(R.string.settings_backup_favs)) },
                        supportingContent = { Text("Export your collection using the built-in file manager") },
                        leadingContent = { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    ListItem(
                        selected = false,
                        onClick = { 
                            viewModel.openFilePicker(isExport = false)
                        },
                        headlineContent = { Text("Restore Favourites") },
                        supportingContent = { Text("Browse local storage using the built-in file manager") },
                        leadingContent = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    ListItem(
                        selected = false,
                        onClick = onPermissionRequest,
                        headlineContent = { Text("Grant Storage Permissions") },
                        supportingContent = { Text("Try this if you cannot see files. Opens system permissions menu.") },
                        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.updateDatabase() },
                        headlineContent = { Text("Force Database Refresh") },
                        supportingContent = {
                            val dateStr = if (lastUpdate > 0) {
                                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastUpdate))
                            } else "Never"
                            Text("Last synced: $dateStr • ${serverStats?.stations ?: "..."} Stations available")
                        },
                        leadingContent = { Icon(Icons.Default.Radio, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.History, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("INTERFACE & CONTENT", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.primary)
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("HomeGenres") },
                        headlineContent = { Text("Home Screen Curation") },
                        supportingContent = { Text("Choose which genres appear on your primary dashboard") },
                        leadingContent = { Icon(Icons.Default.Home, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "PURE RADIO TV", 
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
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
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
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
                Text(if (searchMode == com.toxa.pureradio.ui.viewmodel.SearchMode.Tag) "TAG" else "NAME")
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
                        Text(stringResource(R.string.search_recent), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
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
                                    Text(query)
                                }
                            }
                        }
                    }
                } else {
                    Text(stringResource(R.string.search_empty_hint), modifier = Modifier.padding(top = 16.dp))
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
                                text = selectedSearchTag!!.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
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
fun GenreGroupGrid(groups: List<GenreGroup>, autoFocus: Boolean = true, onGroupClick: (String) -> Unit, onGroupLongClick: ((String) -> Unit)? = null) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus, groups.isNotEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (groups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 140.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(groups, key = { _, group -> group.genreName }) { index, group ->
                GenreGroupCard(
                    group = group.copy(genreName = group.genreName.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }),
                    onClick = { onGroupClick(group.genreName) },
                    onLongClick = if (onGroupLongClick != null) { { onGroupLongClick(group.genreName) } } else null,
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GenreGroupCard(group: GenreGroup, onClick: () -> Unit, onLongClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = group.genreName, 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
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
    val genreLower = genre.lowercase().trim()
    return when {
        genreLower.contains("heavy metal") -> "https://images.unsplash.com/photo-1541614101331-1a5a3a194e90?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("metal") -> "https://images.unsplash.com/photo-1598387181032-a3103a2db5b3?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("punk") -> "https://images.unsplash.com/photo-1583790155708-360d8a5563c0?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("hard rock") -> "https://images.unsplash.com/photo-1521334885634-9552f1055677?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("classic rock") -> "https://images.unsplash.com/photo-1459749411177-042180ce673c?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("rock") -> "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("alternative") || genreLower.contains("indie") -> "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("synthpop") -> "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("pop") && (genreLower.contains("music") || genreLower.contains("hits")) -> "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("pop") || genreLower.contains("hits") || genreLower.contains("top") || genreLower.contains("chart") -> "https://images.unsplash.com/photo-1514525253361-bee8a187449a?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("smooth jazz") -> "https://images.unsplash.com/photo-1525994886773-080587e161c3?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("jazz") -> "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("blues") -> "https://images.unsplash.com/photo-1553034545-31a386996173?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("soul") -> "https://images.unsplash.com/photo-1460723237483-7a6dc9d0b212?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("funk") || genreLower.contains("disco") -> "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("orchestra") || genreLower.contains("symphony") -> "https://images.unsplash.com/photo-1465847899035-1379e576ee5d?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("classical") || genreLower.contains("classic") -> "https://images.unsplash.com/photo-1507838596018-b943e1dd13a9?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("opera") -> "https://images.unsplash.com/photo-1520529125433-21950920427e?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("techno") -> "https://images.unsplash.com/photo-1571266028243-3716f02d2d2e?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("deep house") -> "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("house") -> "https://images.unsplash.com/photo-1557683316-973673baf926?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("trance") -> "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("psytrance") -> "https://images.unsplash.com/photo-1520092352425-9fae9f057c9a?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("electro") || genreLower.contains("edm") -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("ambient") -> "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("chillout") || genreLower.contains("chill") -> "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("lounge") -> "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("country") -> "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("bluegrass") -> "https://images.unsplash.com/photo-1525201548942-d8b8c09ec8d1?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("folk") -> "https://images.unsplash.com/photo-1468164016595-6108e4c60c8b?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("hip hop") -> "https://images.unsplash.com/photo-1520262454473-a1a82276a574?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("rap") || genreLower.contains("urban") || genreLower.contains("r&b") -> "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("reggae") -> "https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("ska") -> "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("world") -> "https://images.unsplash.com/photo-1526218626217-dc65a29bb444?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("latin") -> "https://images.unsplash.com/photo-1525994886773-080587e161c3?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("80s") -> "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("90s") -> "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("70s") -> "https://images.unsplash.com/photo-1516062423079-7ca13cdc7f5a?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("60s") || genreLower.contains("oldies") || genreLower.contains("retro") -> "https://images.unsplash.com/photo-1484755560615-a4c64e99529b?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("soundtrack") || genreLower.contains("movie") || genreLower.contains("film") -> "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("meditation") || genreLower.contains("spiritual") || genreLower.contains("religious") -> "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("news") || genreLower.contains("talk") || genreLower.contains("info") -> "https://images.unsplash.com/photo-1472289065668-ce650ac443d2?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("sport") -> "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("comedy") -> "https://images.unsplash.com/photo-1527224857830-43a7acc85260?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("christmas") || genreLower.contains("xmas") -> "https://images.unsplash.com/photo-1543589077-47d81606c1bf?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("kids") || genreLower.contains("children") -> "https://images.unsplash.com/photo-1516627145497-ae6968895b74?q=80&w=600&auto=format&fit=crop"
        genreLower.contains("lofi") -> "https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=600&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1453090927415-5f45085b65c0?q=80&w=600&auto=format&fit=crop"
    }
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
    val favorites by viewModel.favorites.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
    ) {
        if (stations.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 140.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(stations, key = { _, station -> station.stationUuid }) { index, station ->
                    StationCard(
                        station = station,
                        isFavorite = favorites.contains(station.stationUuid),
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
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isLoading) {
                                    Text("Loading...", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Load More Stations", style = MaterialTheme.typography.labelLarge)
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
fun TagGrid(tags: List<Tag>, autoFocus: Boolean = true, onTagClick: (Tag) -> Unit, onTagLongClick: ((Tag) -> Unit)? = null) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus, tags.isNotEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (tags.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize())
    } else {
        LazyVerticalGrid(
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
                        .then(if (index == 0) Modifier.focusRequester(focusRequester) else Modifier),
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tag.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }, 
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "${tag.stationcount} stations",
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
    onCountryClick: (Country) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus, countries.isNotEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (countries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
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
                        .then(if (index == 0) Modifier.focusRequester(focusRequester) else Modifier),
                    scale = androidx.tv.material3.CardDefaults.scale(focusedScale = 1.1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(getGenreColor(country.name))) {
                        val flagCode = country.iso_3166_1.lowercase().trim()
                        AsyncImage(
                            model = if (flagCode.isNotEmpty()) "https://flagcdn.com/w160/$flagCode.png" else "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?q=80&w=600&auto=format&fit=crop",
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
                            Text(
                                text = country.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "${country.stationcount} stations",
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
        border = androidx.tv.material3.CardDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            )
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
                        model = "https://flagcdn.com/w40/$code.png",
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).align(Alignment.TopStart).padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                if (isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd).padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (isCurrent) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).align(Alignment.BottomEnd).padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = station.votes.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
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
                                    model = "https://flagcdn.com/w80/$code.png",
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
                        val technicalInfo = audioFormat?.let { format ->
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
                            listOfNotNull(codec, kbps, samplerate, channels).joinToString(" ")
                        } ?: "${station.bitrate}k"

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = technicalInfo,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
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
                        
                        val timeMinutes = (playbackTime / 1000) / 60
                        val timeSeconds = (playbackTime / 1000) % 60
                        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", timeMinutes, timeSeconds)
                        
                        Text(
                            text = if (isPlaying) "Playing • $timeStr" else "Paused • $timeStr",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (state.isExport) "Backup Favourites" else "Restore Favourites",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onDismiss) {
                        Text("Close")
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
                                supportingContent = { Text("Go to Parent Directory") },
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
                                    Text("Folder", style = MaterialTheme.typography.labelSmall)
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
                                    text = "Export filename:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
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
                                Text("SAVE HERE")
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        shape = RectangleShape
    ) {
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
                    val technicalInfo = audioFormat?.let { format ->
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
                        listOfNotNull(codec, kbps, samplerate, channels).joinToString(" ")
                    } ?: "${station.bitrate}k"

                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = technicalInfo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
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
                                    model = "https://flagcdn.com/w40/$code.png",
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
                val timeMinutes = (playbackTime / 1000) / 60
                val timeSeconds = (playbackTime / 1000) % 60
                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", timeMinutes, timeSeconds)
                
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Card(onClick = onPrevious, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
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
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.padding(12.dp).size(28.dp)
                    )
                }
                Card(onClick = onNext, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
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
                        contentDescription = "Favorite",
                        modifier = Modifier.padding(10.dp).size(24.dp),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
    }
}

@Composable
fun WaveformAnalyzer(isPlaying: Boolean, modifier: Modifier = Modifier) {
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
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}
