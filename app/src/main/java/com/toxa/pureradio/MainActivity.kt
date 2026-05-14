package com.toxa.pureradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
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

    var stationToManageFav by remember { mutableStateOf<Station?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(enabled = selectedTag != null || selectedCountry != null || settingsSubMenu != null) {
        if (selectedTag != null) viewModel.selectTag(null)
        if (selectedCountry != null) viewModel.selectCountry(null)
        if (settingsSubMenu != null) viewModel.setSettingsSubMenu(null)
    }

    if (stationToManageFav != null) {
        val isFav = favorites.contains(stationToManageFav!!.stationUuid)
        Dialog(onDismissRequest = { stationToManageFav = null }) {
            Surface(
                modifier = Modifier.width(300.dp).padding(16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stationToManageFav!!.name, style = MaterialTheme.typography.headlineSmall)
                    Button(
                        onClick = {
                            viewModel.toggleFavorite(stationToManageFav!!)
                            stationToManageFav = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isFav) "Remove from Favourites" else "Add to Favourites")
                    }
                    Button(
                        onClick = { stationToManageFav = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavigationDrawer(
                drawerState = drawerState,
                drawerContent = { drawerValue ->
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 16.dp, horizontal = 4.dp)
                            .width(if (drawerValue == DrawerValue.Open) 200.dp else 48.dp)
                    ) {
                        NavigationItem.entries.forEach { item ->
                            NavigationDrawerItem(
                                selected = selectedNavItem == item,
                                onClick = { 
                                    viewModel.selectNavigationItem(item)
                                    drawerState.setValue(DrawerValue.Closed)
                                },
                                leadingContent = {
                                    val icon = when (item) {
                                        NavigationItem.Home -> Icons.Default.Home
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            ) {
                                if (drawerValue == DrawerValue.Open) {
                                    Text(
                                        item.name, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val title = when {
                        selectedTag != null -> selectedTag!!.name
                        selectedCountry != null -> selectedCountry!!.name
                        else -> selectedNavItem.name
                    }
                    val isDeepDive = selectedTag != null || selectedCountry != null
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDeepDive) {
                            Surface(
                                colors = androidx.tv.material3.SurfaceDefaults.colors(
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
                            modifier = Modifier.weight(1f)
                        )

                        if (selectedNavItem != NavigationItem.Settings) {
                            BitrateFilters(
                                selectedBitrates = selectedBitrates,
                                onToggleFilter = { viewModel.toggleBitrateFilter(it) }
                            )
                        }

                        if (selectedTag != null || selectedCountry != null) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {
                                viewModel.selectTag(null)
                                viewModel.selectCountry(null)
                            }) {
                                Text("Back")
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (isLoading && selectedNavItem != NavigationItem.Search) {
                            Text(text = "Loading...", style = MaterialTheme.typography.headlineMedium)
                        } else if (error != null) {
                            Text(text = error!!, style = MaterialTheme.typography.headlineMedium)
                        } else {
                            when (selectedNavItem) {
                                NavigationItem.Home -> {
                                    if (genreGroups.isNotEmpty()) {
                                        if (selectedTag == null) {
                                            GenreGroupGrid(genreGroups) { name ->
                                                viewModel.selectTag(tags.find { it.name == name })
                                            }
                                        } else {
                                            StationGrid(stations, viewModel, onLongClick = { stationToManageFav = it })
                                        }
                                    } else {
                                        StationGrid(stations, viewModel, onLongClick = { stationToManageFav = it })
                                    }
                                }
                                NavigationItem.Recent -> {
                                    StationGrid(stations, viewModel, onLongClick = { stationToManageFav = it })
                                }
                                NavigationItem.Favourites -> {
                                    StationGrid(stations, viewModel, onLongClick = { stationToManageFav = it })
                                }
                                NavigationItem.Search -> {
                                    SearchScreen(viewModel, onLongClick = { stationToManageFav = it })
                                }
                                NavigationItem.Genres -> {
                                    if (selectedTag == null) {
                                        TagGrid(tags) { viewModel.selectTag(it) }
                                    } else {
                                        StationGrid(stations, viewModel, onLongClick = { stationToManageFav = it })
                                    }
                                }
                                NavigationItem.Countries -> {
                                    if (selectedCountry == null) {
                                        CountryGrid(countries) { viewModel.selectCountry(it) }
                                    } else {
                                        StationGrid(stations, viewModel, onLongClick = { stationToManageFav = it })
                                    }
                                }
                                NavigationItem.Settings -> {
                                    SettingsScreen(viewModel)
                                }
                                NavigationItem.Exit -> {}
                            }
                        }
                    }
                }
            }
        }

        currentStation?.let { station ->
            val favorites by viewModel.favorites.collectAsState()
            NowPlayingBar(
                station = station,
                isPlaying = isPlaying,
                isFavorite = favorites.contains(station.stationUuid),
                onTogglePlay = { viewModel.togglePlayPause() },
                onToggleFavorite = { viewModel.toggleFavorite(station) },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() }
            )
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
        Text("Bitrate: ", style = MaterialTheme.typography.labelLarge)
        BitrateFilter.entries.forEach { filter ->
            val label = when (filter) {
                BitrateFilter.Low -> "Low (<160)"
                BitrateFilter.Med -> "Med (160-320)"
                BitrateFilter.High -> "High (>320)"
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

    if (settingsSubMenu == "HomeGenres") {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { viewModel.setSettingsSubMenu(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Home Genres Menu", style = MaterialTheme.typography.headlineLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(tags) { tag ->
                ListItem(
                    selected = false,
                    onClick = { viewModel.toggleGenreVisibility(tag.name) },
                    headlineContent = { Text(tag.name) },
                    trailingContent = {
                        Checkbox(checked = visibleGenres.contains(tag.name), onCheckedChange = null)
                    }
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp)) {
            item {
                Text("Settings", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ListItem(
                    selected = false,
                    onClick = { viewModel.setSettingsSubMenu("HomeGenres") },
                    headlineContent = { Text("Home Genres Menu") },
                    supportingContent = { Text("Select genres to display on the Home tab.") },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                )
            }

            item {
                ListItem(
                    selected = false,
                    onClick = { viewModel.toggleHideBroken() },
                    headlineContent = { Text("Hide broken stations") },
                    supportingContent = { Text("Filter out stations that are currently not working.") },
                    trailingContent = {
                        Switch(checked = hideBroken, onCheckedChange = null)
                    }
                )
            }

            item {
                ListItem(
                    selected = false,
                    onClick = { viewModel.updateDatabase() },
                    headlineContent = { Text("Update stations database") },
                    supportingContent = {
                        val dateStr = if (lastUpdate > 0) {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastUpdate))
                        } else "Never"
                        Text("Last update: $dateStr. Total stations: ${serverStats?.stations ?: "..."}")
                    },
                    trailingContent = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("About", style = MaterialTheme.typography.titleLarge)
                        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        Text("Build Date: $buildTime", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(viewModel: MainViewModel, onLongClick: (Station) -> Unit) {
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
                StationGrid(stations, viewModel, onLongClick = onLongClick)
            }
        }
    }
}

@Composable
fun GenreGroupGrid(groups: List<GenreGroup>, onGroupClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(groups) { group ->
            GenreGroupCard(group, onClick = { onGroupClick(group.genreName) })
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GenreGroupCard(group: GenreGroup, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(8.dp).height(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val firstStation = group.stations.firstOrNull()
            AsyncImage(
                model = if (firstStation?.favicon?.isNotEmpty() == true) firstStation.favicon else R.drawable.ic_radio_logo,
                contentDescription = null,
                modifier = Modifier.size(80.dp).padding(8.dp),
                contentScale = ContentScale.Fit,
                error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo),
                placeholder = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = group.genreName, style = MaterialTheme.typography.titleMedium)
            Text(text = "${group.totalStations} stations", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun StationGrid(stations: List<Station>, viewModel: MainViewModel, onLongClick: (Station) -> Unit) {
    val favorites by viewModel.favorites.collectAsState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(stations) { station ->
            StationCard(
                station = station,
                isFavorite = favorites.contains(station.stationUuid),
                onClick = { viewModel.playStation(station) },
                onLongClick = { onLongClick(station) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TagGrid(tags: List<Tag>, onTagClick: (Tag) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tags) { tag ->
            Card(
                onClick = { onTagClick(tag) },
                modifier = Modifier.padding(8.dp).height(80.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = tag.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${tag.stationcount} stations", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CountryGrid(countries: List<Country>, onCountryClick: (Country) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(countries) { country ->
            Card(
                onClick = { onCountryClick(country) },
                modifier = Modifier.padding(8.dp).height(80.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = country.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${country.stationcount} stations", style = MaterialTheme.typography.bodySmall)
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .height(200.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit,
                    error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo),
                    placeholder = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
                )
                if (isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).align(Alignment.TopEnd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NowPlayingBar(
    station: Station,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTogglePlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (station.favicon.isNotEmpty()) station.favicon else R.drawable.ic_radio_logo,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit,
                error = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo),
                placeholder = coil.compose.rememberAsyncImagePainter(R.drawable.ic_radio_logo)
            )
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(onClick = onPrevious, modifier = Modifier.padding(horizontal = 2.dp)) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.padding(6.dp).size(20.dp)
                        )
                    }
                    Card(onClick = onTogglePlay, modifier = Modifier.padding(horizontal = 2.dp)) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.padding(6.dp).size(20.dp)
                        )
                    }
                    Card(onClick = onNext, modifier = Modifier.padding(horizontal = 2.dp)) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.padding(6.dp).size(20.dp)
                        )
                    }
                }
                Text(text = "${station.bitrate} kbps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = station.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = station.country, style = MaterialTheme.typography.bodySmall)
            }
            
            Card(onClick = onToggleFavorite, modifier = Modifier.padding(horizontal = 4.dp)) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    modifier = Modifier.padding(8.dp).size(20.dp),
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
