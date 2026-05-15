package com.toxa.pureradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
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
import androidx.tv.material3.rememberDrawerState
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import androidx.tv.material3.Switch
import coil.compose.AsyncImage
import com.toxa.pureradio.R
import com.toxa.pureradio.data.model.Station
import com.toxa.pureradio.network.Country
import com.toxa.pureradio.network.Tag
import com.toxa.pureradio.ui.theme.PureRadioTheme
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PureRadioTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .onKeyEvent {
                            viewModel.resetScreensaverTimer()
                            false
                        },
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedBitrates by viewModel.selectedBitrates.collectAsState()
    val settingsSubMenu by viewModel.settingsSubMenu.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playbackTime by viewModel.playbackTime.collectAsState()
    val isScreensaverShowing by viewModel.isScreensaverShowing.collectAsState()
    val visibleGenres by viewModel.visibleGenres.collectAsState()

    var stationToFavorite by remember { mutableStateOf<Station?>(null) }
    var isDialogReady by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerFocusRequesters = remember { NavigationItem.entries.associateWith { FocusRequester() } }
    val dialogFocusRequester = remember { FocusRequester() }
    val cancelFocusRequester = remember { FocusRequester() }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Open) {
            try { drawerFocusRequesters[selectedNavItem]?.requestFocus() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(stationToFavorite) {
        if (stationToFavorite != null) {
            isDialogReady = false
            try { cancelFocusRequester.requestFocus() } catch (_: Exception) {}
            // Wait for the user to release the remote button after long press
            delay(800)
            isDialogReady = true
        }
    }

    BackHandler(enabled = true) {
        if (stationToFavorite != null) {
            stationToFavorite = null
        } else if (selectedTag != null) {
            viewModel.selectTag(null)
        } else if (selectedCountry != null) {
            viewModel.selectCountry(null)
        } else if (settingsSubMenu != null) {
            viewModel.setSettingsSubMenu(null)
        } else if (drawerState.currentValue == DrawerValue.Closed) {
            drawerState.setValue(DrawerValue.Open)
            try { drawerFocusRequesters[selectedNavItem]?.requestFocus() } catch (_: Exception) {}
        } else {
            (context as? android.app.Activity)?.finish()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 16.dp, horizontal = 4.dp)
                        .onFocusChanged { 
                            if (it.hasFocus) {
                                drawerState.setValue(DrawerValue.Open)
                            } else {
                                drawerState.setValue(DrawerValue.Closed)
                            }
                        }
                ) {
                    items(NavigationItem.entries) { item ->
                        NavigationDrawerItem(
                            selected = selectedNavItem == item,
                            onClick = { 
                                viewModel.selectNavigationItem(item)
                                drawerState.setValue(DrawerValue.Closed)
                            },
                            modifier = Modifier.focusRequester(drawerFocusRequesters[item]!!),
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
                            Text(
                                item.name, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val title = when {
                    selectedTag != null -> {
                        val name = selectedTag!!.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        "$name (${stations.size} Stations)"
                    }
                    selectedCountry != null -> {
                        val name = selectedCountry!!.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        "$name (${stations.size} Stations)"
                    }
                    else -> selectedNavItem.name
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

                val showBitrateFilters = (selectedNavItem == NavigationItem.Home && selectedTag == null) ||
                                        (selectedNavItem == NavigationItem.Popular) ||
                                        (selectedNavItem == NavigationItem.Genres && selectedTag != null) ||
                                        (selectedNavItem == NavigationItem.Countries && selectedCountry != null)

                    if (showBitrateFilters) {
                        BitrateFilters(
                            selectedBitrates = selectedBitrates,
                            onToggleFilter = { viewModel.toggleBitrateFilter(it) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (error != null) {
                        Text(text = error!!, style = MaterialTheme.typography.headlineMedium)
                    } else {
                        when (selectedNavItem) {
                            NavigationItem.Home -> {
                                val homeGroups = if (visibleGenres.isEmpty()) genreGroups else genreGroups.filter { visibleGenres.contains(it.genreName) }
                                
                                if (homeGroups.isNotEmpty()) {
                                    if (selectedTag == null) {
                                        GenreGroupGrid(homeGroups) { name ->
                                            viewModel.selectTag(tags.find { it.name == name })
                                        }
                                    } else {
                                        StationGrid(stations, viewModel) { stationToFavorite = it }
                                    }
                                } else {
                                    StationGrid(stations, viewModel) { stationToFavorite = it }
                                }
                            }
                            NavigationItem.Popular -> {
                                StationGrid(stations, viewModel) { stationToFavorite = it }
                            }
                            NavigationItem.Recent -> {
                                StationGrid(stations, viewModel) { stationToFavorite = it }
                            }
                            NavigationItem.Favourites -> {
                                StationGrid(stations, viewModel) { stationToFavorite = it }
                            }
                            NavigationItem.Search -> {
                                SearchScreen(viewModel, onLongClick = { stationToFavorite = it })
                            }
                            NavigationItem.Genres -> {
                                if (selectedTag == null) {
                                    TagGrid(tags) { viewModel.selectTag(it) }
                                } else {
                                    StationGrid(stations, viewModel) { stationToFavorite = it }
                                }
                            }
                            NavigationItem.Countries -> {
                                if (selectedCountry == null) {
                                    CountryGrid(countries) { viewModel.selectCountry(it) }
                                } else {
                                    StationGrid(stations, viewModel) { stationToFavorite = it }
                                }
                            }
                            NavigationItem.Settings -> {
                                SettingsScreen(viewModel)
                            }
                            NavigationItem.Exit -> {}
                        }
                    }
                    
                    if (isLoading && selectedNavItem != NavigationItem.Search) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Searching for waves...", 
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        currentStation?.let { station ->
            val playbackDuration by viewModel.playbackDuration.collectAsState()
            NowPlayingBar(
                station = station,
                isPlaying = isPlaying,
                isFavorite = favorites.contains(station.stationUuid),
                playbackTime = playbackTime,
                playbackDuration = playbackDuration,
                onTogglePlay = { viewModel.togglePlayPause() },
                onToggleFavorite = { viewModel.toggleFavorite(station) },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() }
            )
        }
    }

    if (isScreensaverShowing) {
        Screensaver(viewModel)
    }

    stationToFavorite?.let { station ->
        val isAlreadyFavorite = favorites.contains(station.stationUuid)
        Dialog(onDismissRequest = { stationToFavorite = null }) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = SurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAlreadyFavorite) "Remove station from favourites?" else "Add station to favourites?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { if (isDialogReady) stationToFavorite = null },
                            modifier = Modifier.focusRequester(cancelFocusRequester)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (isDialogReady) {
                                    viewModel.toggleFavorite(station)
                                    stationToFavorite = null
                                }
                            },
                            modifier = Modifier.focusRequester(dialogFocusRequester)
                        ) {
                            Text(if (isAlreadyFavorite) "Remove" else "Add")
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
fun SettingsScreen(viewModel: MainViewModel) {
    val visibleGenres by viewModel.visibleGenres.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val settingsSubMenu by viewModel.settingsSubMenu.collectAsState()
    val hideBroken by viewModel.hideBrokenStations.collectAsState()
    val serverStats by viewModel.serverStats.collectAsState()
    val lastUpdate by viewModel.lastDbUpdate.collectAsState()
    val autoUpdateInterval by viewModel.autoUpdateInterval.collectAsState()
    val screensaverEnabled by viewModel.screensaverEnabled.collectAsState()
    val screensaverTimeout by viewModel.screensaverTimeout.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val audioPassthrough by viewModel.audioPassthrough.collectAsState()

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
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Personalize Home Tab", style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(tags) { tag ->
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleGenreVisibility(tag.name) },
                        headlineContent = { 
                            Text(tag.name.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }) 
                        },
                        trailingContent = {
                            Checkbox(checked = visibleGenres.contains(tag.name), onCheckedChange = null)
                        }
                    )
                }
            }
        }
        "AutoUpdate" -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Database Update Interval", style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                val options = listOf(
                    0 to "Manual Only (Off)",
                    12 to "Every 12 Hours",
                    24 to "Every 24 Hours"
                )
                items(options) { (hours, label) ->
                    ListItem(
                        selected = autoUpdateInterval == hours,
                        onClick = { 
                            viewModel.setAutoUpdateInterval(hours)
                            viewModel.setSettingsSubMenu(null)
                        },
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
        "Screensaver" -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setSettingsSubMenu(null) },
                            modifier = Modifier.focusRequester(subMenuFocusRequester)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Screensaver Preferences", style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleScreensaver(!screensaverEnabled) },
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
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
                item {
                    Text("System Settings", style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("INTERFACE & CONTENT", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.primary)
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("HomeGenres") },
                        modifier = Modifier.focusRequester(mainMenuFocusRequester),
                        headlineContent = { Text("Home Screen Curation") },
                        supportingContent = { Text("Choose which genres appear on your primary dashboard") },
                        leadingContent = { Icon(Icons.Default.Home, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("Screensaver") },
                        headlineContent = { Text("Ambient & Screensaver") },
                        supportingContent = {
                            val label = if (screensaverEnabled) "Enabled (${screensaverTimeout}m)" else "Disabled"
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
                        headlineContent = { Text("Audio Passthrough (Bit-Perfect)") },
                        supportingContent = { Text("Experimental: Bypass Android resampler. Note: Ensure 'Match content sample rate' is ON in Shield settings.") },
                        leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = audioPassthrough, onCheckedChange = null)
                        }
                    )
                }

                item {
                    Text("STATION DATABASE", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), color = MaterialTheme.colorScheme.primary)
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleHideBroken() },
                        headlineContent = { Text("Smart Filter") },
                        supportingContent = { Text("Automatically hide stations with reported connection issues") },
                        leadingContent = { Icon(Icons.Default.Public, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = hideBroken, onCheckedChange = null)
                        }
                    )
                }

                item {
                    ListItem(
                        selected = false,
                        onClick = { viewModel.setSettingsSubMenu("AutoUpdate") },
                        headlineContent = { Text("Background Synchronization") },
                        supportingContent = { 
                            val label = when(autoUpdateInterval) {
                                12 -> "12 Hours"
                                24 -> "24 Hours"
                                else -> "Disabled"
                            }
                            Text("Update frequency: $label") 
                        },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
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
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    "Build: 2025-01-30 14:30",
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
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
fun SearchScreen(viewModel: MainViewModel, onLongClick: (Station) -> Unit = {}) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query -> viewModel.onSearchQueryChange(query) },
                label = { Text("Search stations...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            if (isLoading) {
                Spacer(modifier = Modifier.width(16.dp))
                Text("Searching...", style = MaterialTheme.typography.bodySmall)
            }
        }
        
        if (stations.isEmpty() && searchQuery.isEmpty()) {
            Text("Enter search query", modifier = Modifier.padding(top = 16.dp))
        } else {
            Box(modifier = Modifier.weight(1f)) {
                StationGrid(stations, viewModel, autoFocus = false, onLongClick = onLongClick)
            }
        }
    }
}

@Composable
fun GenreGroupGrid(groups: List<GenreGroup>, autoFocus: Boolean = true, onGroupClick: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(groups.isEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (groups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(groups) { index, group ->
                GenreGroupCard(
                    group = group.copy(genreName = group.genreName.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }),
                    onClick = { onGroupClick(group.genreName) },
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GenreGroupCard(group: GenreGroup, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
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
                val countText = if (group.filteredCount < group.stations.size) {
                    "${group.filteredCount} / ${group.totalStations}"
                } else {
                    "${group.totalStations} stations"
                }
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
    onLongClick: (Station) -> Unit = {}
) {
    val favorites by viewModel.favorites.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(stations.isEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    if (stations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(stations) { index, station ->
                StationCard(
                    station = station,
                    isFavorite = favorites.contains(station.stationUuid),
                    isCurrent = currentStation?.stationUuid == station.stationUuid,
                    onClick = { viewModel.playStation(station) },
                    onLongClick = { onLongClick(station) },
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TagGrid(tags: List<Tag>, autoFocus: Boolean = true, onTagClick: (Tag) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(tags.isEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (tags.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(tags) { index, tag ->
                Card(
                    onClick = { onTagClick(tag) },
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
    LaunchedEffect(countries.isEmpty()) {
        if (autoFocus) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }
    if (countries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable())
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(countries) { index, country ->
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun Screensaver(viewModel: MainViewModel) {
    val currentStation by viewModel.currentStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackTime by viewModel.playbackTime.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
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
                        .fillMaxWidth(0.6f)
                        .align(Alignment.Center)
                        .offset(
                            x = (xOffset * 500).dp,
                            y = (yOffset * 300).dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    currentStation?.let { station ->
                        AsyncImage(
                            model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                            contentDescription = null,
                            modifier = Modifier.size(240.dp),
                            contentScale = ContentScale.Fit,
                            error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo),
                            placeholder = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        val timeMinutes = (playbackTime / 1000) / 60
                        val timeSeconds = (playbackTime / 1000) % 60
                        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", timeMinutes, timeSeconds)
                        
                        Text(
                            text = if (isPlaying) "Playing • $timeStr" else "Paused • $timeStr",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        WaveformAnalyzer(isPlaying = isPlaying)
                    }
                }
                
                Text(
                    text = "Press any key to return",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NowPlayingBar(
    station: Station,
    isPlaying: Boolean,
    isFavorite: Boolean,
    playbackTime: Long,
    playbackDuration: Long,
    onTogglePlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
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
                modifier = Modifier.weight(1f),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = station.name, 
                            style = MaterialTheme.typography.titleLarge, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "${station.bitrate}k",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val code = station.countryCode?.trim()?.lowercase()
                        if (!code.isNullOrEmpty() && code.length == 2) {
                            AsyncImage(
                                model = "https://flagcdn.com/w40/$code.png",
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }

                        Text(
                            text = station.country, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
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
