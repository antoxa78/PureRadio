package com.toxa.pureradio.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.toxa.pureradio.data.model.Station
import com.toxa.pureradio.data.repository.RadioRepository
import com.toxa.pureradio.network.Country
import com.toxa.pureradio.network.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

enum class NavigationItem {
    Home, Recent, Search, Genres, Countries, Favourites, Settings, Exit
}

enum class BitrateFilter {
    Low, Med, High
}

data class GenreGroup(
    val genreName: String,
    val stations: List<Station>,
    val totalStations: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RadioRepository()
    private var player: ExoPlayer? = null
    private val prefs = application.getSharedPreferences("pure_radio_prefs", Context.MODE_PRIVATE)

    private val _allStations = MutableStateFlow<List<Station>>(emptyList())
    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations

    private val _genreGroups = MutableStateFlow<List<GenreGroup>>(emptyList())
    val genreGroups: StateFlow<List<GenreGroup>> = _genreGroups

    private val _selectedBitrates = MutableStateFlow<Set<BitrateFilter>>(loadBitrateFilters())
    val selectedBitrates: StateFlow<Set<BitrateFilter>> = _selectedBitrates

    private val _currentStation = MutableStateFlow<Station?>(null)
    val currentStation: StateFlow<Station?> = _currentStation

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedNavItem = MutableStateFlow(NavigationItem.Home)
    val selectedNavItem: StateFlow<NavigationItem> = _selectedNavItem

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries

    private val _selectedTag = MutableStateFlow<Tag?>(null)
    val selectedTag: StateFlow<Tag?> = _selectedTag

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _favorites = MutableStateFlow<Set<String>>(loadFavoritesFromPrefs())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _favoriteStations = MutableStateFlow<List<Station>>(loadFavoriteStationsFromPrefs())

    private val _recentStations = MutableStateFlow<List<Station>>(loadRecentsFromPrefs())
    val recentStations: StateFlow<List<Station>> = _recentStations

    private val _visibleGenres = MutableStateFlow<Set<String>>(loadVisibleGenres())
    val visibleGenres: StateFlow<Set<String>> = _visibleGenres

    private val _hideBrokenStations = MutableStateFlow(loadHideBroken())
    val hideBrokenStations: StateFlow<Boolean> = _hideBrokenStations

    private val _settingsSubMenu = MutableStateFlow<String?>(null)
    val settingsSubMenu: StateFlow<String?> = _settingsSubMenu

    private val _serverStats = MutableStateFlow<com.toxa.pureradio.network.ServerStats?>(null)
    val serverStats: StateFlow<com.toxa.pureradio.network.ServerStats?> = _serverStats

    private val _lastDbUpdate = MutableStateFlow<Long>(prefs.getLong("last_db_update", 0))
    val lastDbUpdate: StateFlow<Long> = _lastDbUpdate

    init {
        player = ExoPlayer.Builder(application).build()
        loadTags()
        loadStats()
        selectNavigationItem(NavigationItem.Home)
    }

    private fun loadStats() {
        viewModelScope.launch {
            _serverStats.value = repository.getStats()
        }
    }

    fun updateDatabase() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stats = repository.getStats()
                if (stats != null) {
                    _serverStats.value = stats
                    _lastDbUpdate.value = System.currentTimeMillis()
                    prefs.edit().putLong("last_db_update", _lastDbUpdate.value).apply()
                }
                _tags.value = repository.getTags()
                _countries.value = repository.getCountries()
            } catch (e: Exception) {
                _error.value = "Failed to update database"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadFavoritesFromPrefs(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    private fun loadFavoriteStationsFromPrefs(): List<Station> {
        val json = prefs.getString("favorite_stations_json", null) ?: return emptyList()
        return try {
            com.google.gson.Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<Station>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavoritesToPrefs(favorites: Set<String>, stations: List<Station>) {
        val json = com.google.gson.Gson().toJson(stations)
        prefs.edit()
            .putStringSet("favorites", favorites)
            .putString("favorite_stations_json", json)
            .apply()
    }

    private fun loadVisibleGenres(): Set<String> {
        return prefs.getStringSet("visible_genres", emptySet()) ?: emptySet()
    }

    private fun saveVisibleGenres(genres: Set<String>) {
        prefs.edit().putStringSet("visible_genres", genres).apply()
    }

    private fun loadHideBroken(): Boolean {
        return prefs.getBoolean("hide_broken", false)
    }

    private fun saveHideBroken(hide: Boolean) {
        prefs.edit().putBoolean("hide_broken", hide).apply()
    }

    private fun loadRecentsFromPrefs(): List<Station> {
        val json = prefs.getString("recent_stations_json", null) ?: return emptyList()
        return try {
            com.google.gson.Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<Station>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveRecentsToPrefs(stations: List<Station>) {
        val json = com.google.gson.Gson().toJson(stations)
        prefs.edit().putString("recent_stations_json", json).apply()
    }

    private fun addToRecent(station: Station) {
        val current = _recentStations.value.toMutableList()
        current.removeAll { it.stationUuid == station.stationUuid }
        current.add(0, station)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _recentStations.value = current
        saveRecentsToPrefs(current)
    }

    fun toggleHideBroken() {
        val newValue = !_hideBrokenStations.value
        _hideBrokenStations.value = newValue
        saveHideBroken(newValue)
        refreshCurrentContent()
    }

    fun setSettingsSubMenu(menu: String?) {
        _settingsSubMenu.value = menu
    }

    fun toggleGenreVisibility(tagName: String) {
        val current = _visibleGenres.value.toMutableSet()
        if (current.contains(tagName)) {
            current.remove(tagName)
        } else {
            current.add(tagName)
        }
        _visibleGenres.value = current
        saveVisibleGenres(current)
        if (_selectedNavItem.value == NavigationItem.Home) {
            loadTopStations()
        }
    }

    private fun refreshCurrentContent() {
        when (_selectedNavItem.value) {
            NavigationItem.Home -> loadTopStations()
            NavigationItem.Genres -> {
                if (_selectedTag.value != null) selectTag(_selectedTag.value)
            }
            NavigationItem.Countries -> {
                if (_selectedCountry.value != null) selectCountry(_selectedCountry.value)
            }
            NavigationItem.Search -> {
                if (_searchQuery.value.length > 2) searchStations(_searchQuery.value)
            }
            else -> {}
        }
    }

    fun selectNavigationItem(item: NavigationItem) {
        if (item == NavigationItem.Exit) {
            exitProcess(0)
        }
        _selectedNavItem.value = item
        _error.value = null
        _selectedTag.value = null
        _selectedCountry.value = null
        _settingsSubMenu.value = null
        when (item) {
            NavigationItem.Home -> loadTopStations()
            NavigationItem.Recent -> {
                _allStations.value = _recentStations.value
                applyFilters()
            }
            NavigationItem.Genres -> loadTags()
            NavigationItem.Countries -> loadCountries()
            NavigationItem.Favourites -> loadFavorites()
            NavigationItem.Search -> {
                _allStations.value = emptyList()
                _stations.value = emptyList()
                _genreGroups.value = emptyList()
            }
            else -> {
                _allStations.value = emptyList()
                _stations.value = emptyList()
                _genreGroups.value = emptyList()
            }
        }
    }

    private fun updateStations(newStations: List<Station>) {
        _allStations.value = newStations
        applyFilters()
    }

    private fun loadBitrateFilters(): Set<BitrateFilter> {
        val saved = prefs.getStringSet("bitrate_filters", null) ?: return emptySet()
        return saved.mapNotNull { 
            try { BitrateFilter.valueOf(it) } catch (e: Exception) { null }
        }.toSet()
    }

    private fun saveBitrateFilters(filters: Set<BitrateFilter>) {
        prefs.edit().putStringSet("bitrate_filters", filters.map { it.name }.toSet()).apply()
    }

    fun toggleBitrateFilter(filter: BitrateFilter) {
        val current = _selectedBitrates.value.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        _selectedBitrates.value = current
        saveBitrateFilters(current)
        applyFilters()
    }

    private fun applyFilters() {
        val bitrates = _selectedBitrates.value
        if (bitrates.isEmpty()) {
            _stations.value = _allStations.value
        } else {
            _stations.value = _allStations.value.filter { station ->
                val br = station.bitrate
                (bitrates.contains(BitrateFilter.Low) && br < 160) ||
                        (bitrates.contains(BitrateFilter.Med) && br >= 160 && br <= 320) ||
                        (bitrates.contains(BitrateFilter.High) && br > 320)
            }
        }
    }

    private fun loadTopStations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val selected = _visibleGenres.value
                val hideBroken = _hideBrokenStations.value
                if (selected.isEmpty()) {
                    _genreGroups.value = emptyList()
                    updateStations(repository.getTopStations(hideBroken = hideBroken))
                } else {
                    val groups = mutableListOf<GenreGroup>()
                    selected.forEach { genre ->
                        val genreStations = repository.searchStations(tag = genre, limit = 250, hideBroken = hideBroken)
                        val total = _tags.value.find { it.name == genre }?.stationcount ?: genreStations.size
                        groups.add(GenreGroup(genre, genreStations, total))
                    }
                    _genreGroups.value = groups
                }
            } catch (e: Exception) {
                _error.value = "Failed to load stations"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            if (_tags.value.isNotEmpty()) return@launch
            try {
                _tags.value = repository.getTags()
            } catch (e: Exception) {
                _error.value = "Failed to load genres"
            }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _countries.value = repository.getCountries()
            } catch (e: Exception) {
                _error.value = "Failed to load countries"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadFavorites() {
        _stations.value = _favoriteStations.value
    }

    fun toggleFavorite(station: Station) {
        val currentFavorites = _favorites.value.toMutableSet()
        val currentStationList = _favoriteStations.value.toMutableList()

        if (currentFavorites.contains(station.stationUuid)) {
            currentFavorites.remove(station.stationUuid)
            currentStationList.removeAll { it.stationUuid == station.stationUuid }
        } else {
            currentFavorites.add(station.stationUuid)
            if (!currentStationList.any { it.stationUuid == station.stationUuid }) {
                currentStationList.add(station)
            }
        }
        _favorites.value = currentFavorites
        _favoriteStations.value = currentStationList
        saveFavoritesToPrefs(currentFavorites, currentStationList)
        
        if (_selectedNavItem.value == NavigationItem.Favourites) {
            _stations.value = currentStationList
        }
    }

    fun selectTag(tag: Tag?) {
        _selectedTag.value = tag
        if (tag == null) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                updateStations(repository.searchStations(tag = tag.name, hideBroken = _hideBrokenStations.value))
            } catch (e: Exception) {
                _error.value = "Failed to load stations for genre"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCountry(country: Country?) {
        _selectedCountry.value = country
        if (country == null) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                updateStations(repository.searchStations(country = country.name, hideBroken = _hideBrokenStations.value))
            } catch (e: Exception) {
                _error.value = "Failed to load stations for country"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.length > 2) {
            searchStations(query)
        }
    }

    fun playStation(station: Station) {
        addToRecent(station)
        _currentStation.value = station
        player?.let {
            it.stop()
            it.clearMediaItems()
            val mediaItem = MediaItem.fromUri(station.url)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
            _isPlaying.value = true
        }
    }

    fun playNext() {
        val current = _currentStation.value ?: return
        val list = if (_selectedNavItem.value == NavigationItem.Home && _visibleGenres.value.isNotEmpty()) {
            _genreGroups.value.flatMap { it.stations }
        } else {
            _stations.value
        }
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.stationUuid == current.stationUuid }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            playStation(list[nextIndex])
        }
    }

    fun playPrevious() {
        val current = _currentStation.value ?: return
        val list = if (_selectedNavItem.value == NavigationItem.Home && _visibleGenres.value.isNotEmpty()) {
            _genreGroups.value.flatMap { it.stations }
        } else {
            _stations.value
        }
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.stationUuid == current.stationUuid }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            playStation(list[prevIndex])
        }
    }

    fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.play()
                _isPlaying.value = true
            }
        }
    }

    fun stopPlayback() {
        player?.stop()
        _isPlaying.value = false
        _currentStation.value = null
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                updateStations(repository.searchStations(query = query, hideBroken = _hideBrokenStations.value))
            } catch (e: Exception) {
                _error.value = "Search failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}
