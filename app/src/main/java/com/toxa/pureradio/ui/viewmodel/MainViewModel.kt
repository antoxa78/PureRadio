package com.toxa.pureradio.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.toxa.pureradio.service.PlaybackService
import com.toxa.pureradio.data.model.Station
import com.toxa.pureradio.data.repository.RadioRepository
import com.toxa.pureradio.network.Country
import com.toxa.pureradio.network.ServerStats
import com.toxa.pureradio.network.Tag
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

enum class NavigationItem {
    Home, Popular, Recent, Search, Genres, Countries, Favourites, Settings, Exit
}

enum class BitrateFilter {
    Low, High, FLAC
}

data class GenreGroup(
    val genreName: String,
    val stations: List<Station>,
    val totalStations: Int = 0,
    val filteredCount: Int = 0,
    val isCountry: Boolean = false
)

/** Home-screen categories (genre/country sections shown on the Home tab), if a backup contained any. */
data class PendingHomeSettings(
    val genres: Set<String>,
    val countries: Set<String>
)

/** Result of parsing a playlist/backup file: favourite stations plus optional home curation. */
data class ParsedBackup(
    val stations: List<Station>,
    val homeSettings: PendingHomeSettings?
)

/** A generated backup file ready to be handed to a file manager's "Save to..." target. */
data class BackupShareRequest(
    val uri: Uri,
    val fileName: String,
    /** Set when a specific app (File Manager+) should be launched instead of a chooser. */
    val target: ComponentName? = null
)

enum class ScreensaverMode {
    StationInfo, BlackScreen
}

enum class GenreSortMode {
    Name, Count
}

enum class SearchMode {
    Name, Tag
}

enum class AppTheme {
    ModernBlue, RetroGold, BlueNeon, Violet, Monochrome, Forest, Contrast,
    OledCrimson, OledEmerald, OledMagenta, OledAmber
}

enum class AppLanguage {
    English, Russian, Ukrainian
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RadioRepository()
    private var player: ExoPlayer? = null
    private var playbackService: PlaybackService? = null
    private var serviceBound = false
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

    private val _playbackTime = MutableStateFlow(0L)
    val playbackTime: StateFlow<Long> = _playbackTime

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _selectedNavItem = MutableStateFlow(NavigationItem.Home)
    val selectedNavItem: StateFlow<NavigationItem> = _selectedNavItem

    private val _genreSortMode = MutableStateFlow(GenreSortMode.Count)
    val genreSortMode: StateFlow<GenreSortMode> = _genreSortMode

    private val _minTagFilter = MutableStateFlow(prefs.getBoolean("min_tag_filter", true))
    val minTagFilter: StateFlow<Boolean> = _minTagFilter

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = combine(_tags, _genreSortMode) { tags, mode ->
        when (mode) {
            GenreSortMode.Name -> tags.sortedBy { it.name.lowercase() }
            GenreSortMode.Count -> tags.sortedByDescending { it.stationcount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries

    private val _selectedTag = MutableStateFlow<Tag?>(null)
    val selectedTag: StateFlow<Tag?> = _selectedTag

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry

    private val _lastBrowsedCategory = MutableStateFlow<String?>(null)
    val lastBrowsedCategory: StateFlow<String?> = _lastBrowsedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchMode = MutableStateFlow(SearchMode.Name)
    val searchMode: StateFlow<SearchMode> = _searchMode

    private val _tagSearchGroups = MutableStateFlow<List<GenreGroup>>(emptyList())
    val tagSearchGroups: StateFlow<List<GenreGroup>> = _tagSearchGroups

    private val _selectedSearchTag = MutableStateFlow<String?>(null)
    val selectedSearchTag: StateFlow<String?> = _selectedSearchTag

    private val _recentSearches = MutableStateFlow<List<String>>(loadRecentSearches())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    private val _tagSearchQuery = MutableStateFlow("")
    val tagSearchQuery: StateFlow<String> = _tagSearchQuery

    val filteredTags: StateFlow<List<Tag>> = combine(_tags, _tagSearchQuery, _genreSortMode, _selectedBitrates, _minTagFilter) { tags, query, mode, bitrates, minFilter ->
        val sorted = when (mode) {
            GenreSortMode.Name -> tags.sortedBy { it.name.lowercase() }
            GenreSortMode.Count -> tags.sortedByDescending { it.stationcount }
        }
        val filteredByQuery = if (query.isBlank()) sorted
        else sorted.filter { it.name.contains(query, ignoreCase = true) }
        
        val filteredByMin = if (minFilter) filteredByQuery.filter { it.stationcount >= 101 }
        else filteredByQuery

        filteredByMin
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _favorites = MutableStateFlow<Set<String>>(loadFavoritesFromPrefs())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _favoriteStations = MutableStateFlow<List<Station>>(loadFavoriteStationsFromPrefs())

    private val _recentStations = MutableStateFlow<List<Station>>(loadRecentsFromPrefs())
    val recentStations: StateFlow<List<Station>> = _recentStations

    private val _visibleGenres = MutableStateFlow<Set<String>>(loadVisibleGenres())
    val visibleGenres: StateFlow<Set<String>> = _visibleGenres

    private val _visibleCountries = MutableStateFlow<Set<String>>(loadVisibleCountries())
    val visibleCountries: StateFlow<Set<String>> = _visibleCountries

    private val _hideBrokenStations = MutableStateFlow(loadHideBroken())
    val hideBrokenStations: StateFlow<Boolean> = _hideBrokenStations

    private val _stationOffset = MutableStateFlow(0)
    private val _hasMoreStations = MutableStateFlow(true)
    val hasMoreStations: StateFlow<Boolean> = _hasMoreStations

    private val _settingsSubMenu = MutableStateFlow<String?>(null)
    val settingsSubMenu: StateFlow<String?> = _settingsSubMenu

    private val _serverStats = MutableStateFlow<ServerStats?>(null)
    val serverStats: StateFlow<ServerStats?> = _serverStats

    private val _lastDbUpdate = MutableStateFlow<Long>(prefs.getLong("last_db_update", 0))
    val lastDbUpdate: StateFlow<Long> = _lastDbUpdate

    private val _autoUpdateInterval = MutableStateFlow(prefs.getInt("auto_update_interval", 24))
    val autoUpdateInterval: StateFlow<Int> = _autoUpdateInterval

    private val _screensaverEnabled = MutableStateFlow(prefs.getBoolean("screensaver_enabled", true))
    val screensaverEnabled: StateFlow<Boolean> = _screensaverEnabled

    private val _screensaverTimeout = MutableStateFlow(prefs.getInt("screensaver_timeout", 1))
    val screensaverTimeout: StateFlow<Int> = _screensaverTimeout

    private val _screensaverMode = MutableStateFlow(prefs.getString("screensaver_mode", ScreensaverMode.StationInfo.name)?.let {
        try { ScreensaverMode.valueOf(it) } catch (e: Exception) { ScreensaverMode.StationInfo }
    } ?: ScreensaverMode.StationInfo)
    val screensaverMode: StateFlow<ScreensaverMode> = _screensaverMode

    private val _isScreensaverShowing = MutableStateFlow(false)
    val isScreensaverShowing: StateFlow<Boolean> = _isScreensaverShowing

    private val _pendingImportStations = MutableStateFlow<List<Station>?>(null)
    val pendingImportStations: StateFlow<List<Station>?> = _pendingImportStations

    private val _pendingHomeSettings = MutableStateFlow<PendingHomeSettings?>(null)
    val pendingHomeSettings: StateFlow<PendingHomeSettings?> = _pendingHomeSettings

    private val _pendingOverwriteFile = MutableStateFlow<File?>(null)
    val pendingOverwriteFile: StateFlow<File?> = _pendingOverwriteFile

    // Set after a backup is written to the cache; the Activity shares it with a file manager
    // and then clears this again.
    private val _backupShareRequest = MutableStateFlow<BackupShareRequest?>(null)
    val backupShareRequest: StateFlow<BackupShareRequest?> = _backupShareRequest

    private val _filePickerState = MutableStateFlow<FilePickerState?>(null)
    val filePickerState: StateFlow<FilePickerState?> = _filePickerState

    private val _mediaMetadata = MutableStateFlow<androidx.media3.common.MediaMetadata?>(null)
    val mediaMetadata: StateFlow<androidx.media3.common.MediaMetadata?> = _mediaMetadata

    private val _audioFormat = MutableStateFlow<androidx.media3.common.Format?>(null)
    val audioFormat: StateFlow<androidx.media3.common.Format?> = _audioFormat

    private val _audioPassthrough = MutableStateFlow(prefs.getBoolean("audio_passthrough", false))
    val audioPassthrough: StateFlow<Boolean> = _audioPassthrough

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val _appTheme = MutableStateFlow(loadAppTheme())
    val appTheme: StateFlow<AppTheme> = _appTheme

    private val _quitConfirmationEnabled = MutableStateFlow(prefs.getBoolean("quit_confirmation", true))
    val quitConfirmationEnabled: StateFlow<Boolean> = _quitConfirmationEnabled

    private val _defaultCategory = MutableStateFlow(loadDefaultCategory())
    val defaultCategory: StateFlow<NavigationItem> = _defaultCategory

    private val _appLanguage = MutableStateFlow(loadAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    private val _autoReconnectEnabled = MutableStateFlow(prefs.getBoolean("auto_reconnect", true))
    val autoReconnectEnabled: StateFlow<Boolean> = _autoReconnectEnabled

    private val _extraBufferingEnabled = MutableStateFlow(prefs.getBoolean("extra_buffering", false))

    private val _bestStreamEnabled = MutableStateFlow(prefs.getBoolean("best_stream" /* = PlaybackService.PREF_BEST_STREAM */, true))
    val bestStreamEnabled: StateFlow<Boolean> = _bestStreamEnabled

    // UUID of the list entry the user (or auto-advance) picked; differs from currentStation's
    // UUID when a better stream of that station is playing instead.
    private val _playingFromUuid = MutableStateFlow<String?>(null)
    val playingFromUuid: StateFlow<String?> = _playingFromUuid
    val extraBufferingEnabled: StateFlow<Boolean> = _extraBufferingEnabled

    private var lastInteractionTime = System.currentTimeMillis()
    private var consecutiveErrors = 0
    private var stationRetryCount = 0
    private var retryJob: kotlinx.coroutines.Job? = null
    // Pending "better stream" lookup started by playStation(); cancelled when another station
    // is chosen or playback is stopped.
    private var streamLookupJob: kotlinx.coroutines.Job? = null
    // Clears the retry budget only after playback has stayed up for a while — resetting it
    // the instant a reconnect succeeds would let a rapidly flapping stream retry forever.
    private var retryResetJob: kotlinx.coroutines.Job? = null
    private var searchJob: kotlinx.coroutines.Job? = null
    private var contentRequestId = 0L

    // Maximum API pages fetched per Home category when a bitrate filter is active.
    private val maxHomeGroupPages = 2

    // How many per-category Home requests are allowed in-flight at once, to avoid
    // tripping radio-browser rate limiting when many categories are selected.
    private val maxHomeRequestsInFlight = 6

    private fun beginContentRequest(): Long {
        contentRequestId++
        return contentRequestId
    }

    private fun isCurrentContentRequest(requestId: Long): Boolean =
        requestId == contentRequestId

    // NOTE: the ViewModel's start-up work lives in the single `init` block at the very END of
    // this class. Kotlin runs property initializers and init blocks in textual order, so an
    // init placed up here would run while properties declared further down (e.g.
    // _searchFocusTrigger, loadingMoreStations, playerListener) are still null — which crashed
    // the app on launch when the start-up category was set to Search.

    /**
     * Shared between the initial player and any player rebuilt later by
     * [reinitializePlayer] (toggling Extra Buffering / Audio Passthrough swaps in a new
     * ExoPlayer instance, which starts with no listeners attached).
     */
    private val playerListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            val station = _currentStation.value
            if (_autoReconnectEnabled.value && station != null && stationRetryCount < 3) {
                stationRetryCount++
                _isPlaying.value = false
                retryJob?.cancel()
                retryJob = viewModelScope.launch {
                    for (i in 5 downTo 1) {
                        _infoMessage.value = getApplication<Application>().getString(com.toxa.pureradio.R.string.error_connection_lost, i, stationRetryCount)
                        delay(1000)
                    }
                    _infoMessage.value = getApplication<Application>().getString(com.toxa.pureradio.R.string.error_retrying_now)
                    playStation(station, resetErrors = false)
                }
            } else {
                consecutiveErrors++
                stationRetryCount = 0
                if (consecutiveErrors < 5) {
                    playNext(isAuto = true)
                } else {
                    _error.value = "Playback failed: ${error.message}"
                    _infoMessage.value = null
                    stopPlayback()
                    consecutiveErrors = 0
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            retryResetJob?.cancel()
            if (isPlaying) {
                consecutiveErrors = 0
                _error.value = null
                _infoMessage.value = null
                // Only clear the auto-reconnect budget once the stream has proven itself
                // stable for a while — see the comment on retryResetJob above.
                retryResetJob = viewModelScope.launch {
                    delay(30_000)
                    stationRetryCount = 0
                }
            }
            _isPlaying.value = isPlaying
        }

        override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
            _mediaMetadata.value = mediaMetadata
        }

        override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
            // Try to extract format info from the active audio track
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_AUDIO && group.isSelected) {
                    for (i in 0 until group.length) {
                        if (group.isTrackSelected(i)) {
                            _audioFormat.value = group.getTrackFormat(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private val playbackServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? PlaybackService.LocalBinder ?: return
            val service = localBinder.getService()
            playbackService = service
            serviceBound = true
            val connectedPlayer = service.getPlayer()
            player = connectedPlayer
            connectedPlayer?.addListener(playerListener)
            connectedPlayer?.let { restoreStateFromPlayer(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            player?.removeListener(playerListener)
            player = null
            playbackService = null
            serviceBound = false
        }
    }

    /**
     * The service (and its player) outlive this ViewModel: audio keeps playing after the
     * Activity is closed (PiP window dismissed, app swiped away, Activity destroyed by the
     * system). When the app is opened again a fresh ViewModel binds to that still-running
     * player, so pull the current station / play state back from it — otherwise the UI shows
     * nothing playing while audio is audible, with no Now Playing bar to stop it.
     */
    private fun restoreStateFromPlayer(connectedPlayer: ExoPlayer) {
        if (_currentStation.value != null) return
        val item = connectedPlayer.currentMediaItem ?: return
        if (connectedPlayer.playbackState == Player.STATE_IDLE) return
        val uuid = item.mediaId
        // playStation() always puts the station at the top of Recent, so it is normally there.
        val station = _recentStations.value.firstOrNull { it.stationUuid == uuid }
            ?: _favoriteStations.value.firstOrNull { it.stationUuid == uuid }
            ?: Station(
                stationUuid = uuid,
                name = item.mediaMetadata.title?.toString().orEmpty(),
                url = item.localConfiguration?.uri?.toString().orEmpty(),
                favicon = "",
                tags = "",
                country = "",
                countryCode = null,
                language = "",
                votes = 0,
                codec = "",
                bitrate = 0
            )
        _currentStation.value = station
        _isPlaying.value = connectedPlayer.isPlaying
        _mediaMetadata.value = connectedPlayer.mediaMetadata
        playerListener.onTracksChanged(connectedPlayer.currentTracks)
    }

    /**
     * Binds to [PlaybackService], which owns the real ExoPlayer + MediaSession, so playback
     * keeps running (with a proper foreground-service notification) even after this
     * ViewModel's Activity leaves the foreground. Binding alone doesn't make the service a
     * foreground service — that only happens once [playStation] starts the service — so
     * this is safe to call unconditionally on init without risking an ANR.
     */
    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val context = getApplication<Application>()
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_LOCAL_BIND
        }
        context.bindService(
            intent,
            playbackServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    /** Rebuilds the ExoPlayer inside the service with new buffering/passthrough settings. */
    private fun reinitializePlayer() {
        val service = playbackService ?: return
        player?.removeListener(playerListener)
        val newPlayer = service.buildPlayer(
            extraBuffering = _extraBufferingEnabled.value,
            audioPassthrough = _audioPassthrough.value
        )
        newPlayer.addListener(playerListener)
        player = newPlayer
        _isPlaying.value = newPlayer.isPlaying
    }

    fun toggleAudioPassthrough() {
        val newValue = !_audioPassthrough.value
        _audioPassthrough.value = newValue
        prefs.edit().putBoolean("audio_passthrough", newValue).apply()
        reinitializePlayer()
    }

    private fun startScreensaverTimer() {
        viewModelScope.launch {
            while (true) {
                if (_screensaverEnabled.value && _isPlaying.value && !_isScreensaverShowing.value) {
                    val idleTime = System.currentTimeMillis() - lastInteractionTime
                    if (idleTime > _screensaverTimeout.value * 60 * 1000L) {
                        _isScreensaverShowing.value = true
                    }
                }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    fun resetScreensaverTimer() {
        lastInteractionTime = System.currentTimeMillis()
        if (_isScreensaverShowing.value) {
            _isScreensaverShowing.value = false
        }
    }

    private fun checkAutoUpdate() {
        val intervalHours = _autoUpdateInterval.value
        if (intervalHours > 0) {
            val lastUpdate = _lastDbUpdate.value
            val now = System.currentTimeMillis()
            val intervalMillis = intervalHours * 60 * 60 * 1000L
            if (now - lastUpdate > intervalMillis) {
                updateDatabase()
            }
        }
    }

    fun setAutoUpdateInterval(hours: Int) {
        _autoUpdateInterval.value = hours
        prefs.edit().putInt("auto_update_interval", hours).apply()
        if (hours > 0) checkAutoUpdate()
    }

    fun toggleScreensaver(enabled: Boolean) {
        _screensaverEnabled.value = enabled
        prefs.edit().putBoolean("screensaver_enabled", enabled).apply()
    }

    fun setScreensaverTimeout(minutes: Int) {
        _screensaverTimeout.value = minutes
        prefs.edit().putInt("screensaver_timeout", minutes).apply()
    }

    fun setScreensaverMode(mode: ScreensaverMode) {
        _screensaverMode.value = mode
        prefs.edit().putString("screensaver_mode", mode.name).apply()
    }

    data class FilePickerState(
        val currentPath: String,
        val rootPath: String,
        val files: List<File>,
        val isExport: Boolean,
        val suggestedFileName: String = ""
    )

    fun openFilePicker(isExport: Boolean, suggestedFileName: String = "") {
        val root = Environment.getExternalStorageDirectory() ?: File("/")
        val musicDir = File(root, "Music")
        if (!musicDir.exists()) musicDir.mkdirs()
        val initialDir = if (musicDir.exists() && musicDir.isDirectory) musicDir else root
        
        _filePickerState.value = FilePickerState(
            currentPath = initialDir.absolutePath,
            rootPath = root.absolutePath,
            files = listFiles(initialDir),
            isExport = isExport,
            suggestedFileName = suggestedFileName
        )
    }

    fun getTimestampedBackupFileName(): String {
        val now = java.util.Date()
        val format = java.text.SimpleDateFormat("dd_MMM_yyyy_HH.mm", java.util.Locale.US)
        return "pureradio backup ${format.format(now).lowercase()}.m3u"
    }

    fun closeFilePicker() {
        _filePickerState.value = null
    }

    fun navigateInFilePicker(directory: File) {
        val currentState = _filePickerState.value ?: return
        _filePickerState.value = currentState.copy(
            currentPath = directory.absolutePath,
            files = listFiles(directory)
        )
    }

    fun navigateUpFilePicker() {
        val currentState = _filePickerState.value ?: return
        val currentFile = File(currentState.currentPath)
        if (currentFile.absolutePath == currentState.rootPath) return
        val parent = currentFile.parentFile ?: return
        _filePickerState.value = currentState.copy(
            currentPath = parent.absolutePath,
            files = listFiles(parent)
        )
    }

    private fun listFiles(directory: File): List<File> {
        val files = directory.listFiles()
        if (files == null) {
            _error.value = "Permission Denied: Cannot access ${directory.name}. Ensure storage permissions are granted in System Settings."
            return emptyList()
        }
        return files.toList()
            .filter { !it.name.startsWith(".") }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    fun handleFileSelection(file: File) {
        val currentState = _filePickerState.value ?: return
        if (currentState.isExport) {
            val targetFile = if (file.isDirectory) {
                File(file, currentState.suggestedFileName)
            } else {
                file
            }
            if (targetFile.exists()) {
                _pendingOverwriteFile.value = targetFile
            } else {
                saveFavoritesToFile(targetFile)
                closeFilePicker()
            }
        } else {
            if (!file.isDirectory) {
                prepareImportFavoritesFromFile(file)
            }
        }
    }

    private fun prepareImportFavoritesFromFile(file: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val parsed = withContext(Dispatchers.IO) {
                    parsePlaylistLines(file.readLines())
                }
                
                if (parsed.stations.isNotEmpty() || parsed.homeSettings != null) {
                    _pendingImportStations.value = parsed.stations
                    _pendingHomeSettings.value = parsed.homeSettings
                    // Don't close file picker yet if we need dialog, or close it and show dialog?
                    // Better to close file picker and show the restore dialog on top of settings.
                    closeFilePicker()
                } else {
                    _error.value = "No stations found in file"
                    closeFilePicker()
                }
            } catch (e: Exception) {
                _error.value = "Import failed: ${e.message}"
                closeFilePicker()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmRestore(replace: Boolean) {
        val stations = _pendingImportStations.value ?: emptyList()
        val homeSettings = _pendingHomeSettings.value
        if (stations.isEmpty() && homeSettings == null) {
            _pendingImportStations.value = null
            _pendingHomeSettings.value = null
            return
        }
        if (replace) {
            _favorites.value = emptySet()
            _favoriteStations.value = emptyList()
        }
        mergeImportedStations(stations)
        applyHomeSettings(homeSettings, replace = replace)
        _pendingImportStations.value = null
        _pendingHomeSettings.value = null
        val parts = buildList {
            if (stations.isNotEmpty()) add("${stations.size} stations")
            homeSettings?.let { add("${it.genres.size + it.countries.size} home categories") }
        }
        _successMessage.value = "Imported ${parts.joinToString(" and ")}"
        refreshFavoriteStations()
    }

    fun cancelRestore() {
        _pendingImportStations.value = null
        _pendingHomeSettings.value = null
    }

    fun confirmOverwrite() {
        val file = _pendingOverwriteFile.value ?: return
        _pendingOverwriteFile.value = null
        saveFavoritesToFile(file)
        closeFilePicker()
    }

    fun cancelOverwrite() {
        _pendingOverwriteFile.value = null
    }

    private fun saveFavoritesToFile(file: File) {
        viewModelScope.launch {
            try {
                val content = generateM3uContent()
                withContext(Dispatchers.IO) {
                    file.writeText(content)
                }
                _successMessage.value = "Favourites exported to ${file.name}"
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    /**
     * Percent-encodes a value for use inside the `;`-and-`=`-delimited
     * `#EXT-X-PURE-RADIO-DATA:` line, so a station name/tag/etc. containing a literal `;`
     * or `=` can't desync the key/value pairs on re-import. Paired with [decodePlaylistField].
     */
    private fun encodePlaylistField(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")

    /** Reverses [encodePlaylistField], tolerating plain (older, un-encoded) values. */
    private fun decodePlaylistField(value: String): String =
        try { java.net.URLDecoder.decode(value, "UTF-8") } catch (e: Exception) { value }

    private fun generateM3uContent(): String {
        val stations = _favoriteStations.value
        val content = StringBuilder("#EXTM3U\n")

        val genres = _visibleGenres.value
        val countries = _visibleCountries.value
        if (genres.isNotEmpty() || countries.isNotEmpty()) {
            // Custom tag for home-screen curation (the genre/country sections on the Home
            // tab). Each name is percent-encoded separately and the encoded values joined
            // with ',', so a name containing ',' or ';' (e.g. "Korea, Republic Of") can't
            // desync the list on re-import.
            content.append(
                "#EXT-X-PURE-RADIO-HOME:" +
                    "genres=${genres.joinToString(",") { element -> encodePlaylistField(element) }};" +
                    "countries=${countries.joinToString(",") { element -> encodePlaylistField(element) }}\n"
            )
        }

        stations.forEach { station ->
            // Commas delimit the title in the standard EXTINF format, so a comma in the
            // station name would otherwise get read back by other players (and our own
            // fallback parsing) as ending the title early.
            val extinfSafeName = station.name.replace(",", " ").replace("\n", " ")
            content.append("#EXTINF:-1 tvg-id=\"${station.stationUuid}\" tvg-logo=\"${station.favicon}\" group-title=\"${station.tags}\" tvg-country=\"${station.countryCode ?: ""}\",$extinfSafeName\n")
            // Custom tag for full station metadata recovery. Every field is percent-encoded
            // so a name/tag containing ';' or '=' can't corrupt the other fields on import.
            content.append(
                "#EXT-X-PURE-RADIO-DATA:" +
                    "uuid=${encodePlaylistField(station.stationUuid)};" +
                    "name=${encodePlaylistField(station.name)};" +
                    "favicon=${encodePlaylistField(station.favicon)};" +
                    "tags=${encodePlaylistField(station.tags)};" +
                    "country=${encodePlaylistField(station.country)};" +
                    "countrycode=${encodePlaylistField(station.countryCode ?: "")};" +
                    "lang=${encodePlaylistField(station.language)};" +
                    "votes=${station.votes};" +
                    "codec=${encodePlaylistField(station.codec ?: "")};" +
                    "bitrate=${station.bitrate}\n"
            )
            content.append("${station.url}\n")
        }
        return content.toString()
    }

    private fun parsePlaylistLines(lines: List<String>): ParsedBackup {
        if (lines.isEmpty()) return ParsedBackup(emptyList(), null)
        
        val firstLine = lines.firstOrNull { it.isNotBlank() }?.trim()?.removePrefix("\uFEFF") ?: return ParsedBackup(emptyList(), null)
        
        return when {
            firstLine.startsWith("#EXTM3U", ignoreCase = true) -> parseM3uLines(lines)
            // A combined backup may contain only home curation (no stations). Route it to the
            // M3U parser directly so its HOME tag isn't mistaken for a bare PLS key=value file.
            firstLine.startsWith("#EXT-X-PURE-RADIO-HOME:", ignoreCase = true) -> parseM3uLines(lines)
            firstLine.startsWith("[playlist]", ignoreCase = true) -> parsePlsLines(lines)
            firstLine.contains("=", ignoreCase = false) && firstLine.any { it.isDigit() } -> {
                // Might be a PLS file without header (e.g. File1=url or URL1=url)
                parsePlsLines(lines)
            }
            else -> {
                // Try M3U even if no header, or just plain list of URLs
                parseM3uLines(lines)
            }
        }
    }

    private fun parsePlsLines(lines: List<String>): ParsedBackup {
        val importedStations = mutableListOf<Station>()
        val entries = mutableMapOf<Int, MutableMap<String, String>>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("[") || trimmedLine.startsWith("#")) continue
            
            val parts = trimmedLine.split("=", limit = 2)
            if (parts.size < 2) continue
            
            val key = parts[0].trim()
            val value = parts[1].trim()
            
            // Expected keys: FileN, TitleN, LengthN
            val keyName = key.filter { it.isLetter() }.lowercase()
            val index = key.filter { it.isDigit() }.toIntOrNull() ?: continue
            
            val entry = entries.getOrPut(index) { mutableMapOf() }
            entry[keyName] = value
        }
        
        entries.keys.sorted().forEach { index ->
            val entry = entries[index] ?: return@forEach
            val url = entry["file"] ?: entry["url"] ?: return@forEach
            val name = entry["title"] ?: url.substringAfterLast("/").substringBefore("?")
            
            importedStations.add(Station(
                stationUuid = java.util.UUID.randomUUID().toString(),
                name = name,
                url = url,
                favicon = "",
                tags = "",
                country = "",
                countryCode = null,
                language = "",
                votes = 0,
                codec = "",
                bitrate = 0
            ))
        }
        
        return ParsedBackup(importedStations, null)
    }

    private fun parseM3uLines(lines: List<String>): ParsedBackup {
        val importedStations = mutableListOf<Station>()
        var currentName = ""
        var currentUuid = ""
        var currentFavicon = ""
        var currentTags = ""
        var currentCountry = ""
        var currentCountryCode = ""
        var currentLanguage = ""
        var currentVotes = 0
        var currentCodec = ""
        var currentBitrate = 0
        var homeGenresRaw = ""
        var homeCountriesRaw = ""
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("#EXT-X-PURE-RADIO-HOME:") -> {
                    // Home-screen curation carried by backups made with this version.
                    // Like the per-station data line, values are percent-encoded on export
                    // so a name containing ';' or '=' can't be mistaken for a separator.
                    val data = trimmedLine.removePrefix("#EXT-X-PURE-RADIO-HOME:").split(";")
                    val map = data.associate {
                        val parts = it.split("=", limit = 2)
                        // Keep values URL-encoded here and decode AFTER splitting on ',', so
                        // a literal comma inside a name (encoded as %2C) isn't taken for the
                        // list separator.
                        if (parts.size == 2) parts[0] to parts[1] else "" to ""
                    }
                    homeGenresRaw = map["genres"] ?: ""
                    homeCountriesRaw = map["countries"] ?: ""
                }
                trimmedLine.startsWith("#EXT-X-PURE-RADIO-DATA:") -> {
                    // Fields are percent-encoded on export (see generateM3uContent) so a
                    // ';' or '=' inside a value can't be mistaken for a field separator.
                    val data = trimmedLine.removePrefix("#EXT-X-PURE-RADIO-DATA:").split(";")
                    val map = data.associate {
                        val parts = it.split("=", limit = 2)   // limit=2 preserves = inside URLs/values
                        if (parts.size == 2) parts[0] to decodePlaylistField(parts[1]) else "" to ""
                    }
                    currentUuid = map["uuid"] ?: ""
                    currentName = map["name"] ?: ""
                    currentFavicon = map["favicon"] ?: ""
                    currentTags = map["tags"] ?: ""
                    currentCountry = map["country"] ?: ""
                    currentCountryCode = map["countrycode"] ?: ""
                    currentLanguage = map["lang"] ?: ""
                    currentVotes = map["votes"]?.toIntOrNull() ?: 0
                    currentCodec = map["codec"] ?: ""
                    currentBitrate = map["bitrate"]?.toIntOrNull() ?: 0
                }
                trimmedLine.startsWith("#EXTINF:") -> {
                    if (currentUuid.isEmpty()) {
                        val regex = "tvg-id=\"([^\"]*)\"".toRegex()
                        currentUuid = regex.find(trimmedLine)?.groupValues?.get(1) ?: ""
                    }
                    if (currentFavicon.isEmpty()) {
                        val regex = "tvg-logo=\"([^\"]*)\"".toRegex()
                        currentFavicon = regex.find(trimmedLine)?.groupValues?.get(1) ?: ""
                    }
                    val namePart = trimmedLine.split(",").lastOrNull() ?: ""
                    if (currentName.isEmpty()) currentName = namePart.trim()
                }
                !trimmedLine.startsWith("#") && trimmedLine.isNotBlank() -> {
                    val url = trimmedLine
                    val name = if (currentName.isNotEmpty()) currentName else url.substringAfterLast("/").substringBefore("?")
                    
                    importedStations.add(Station(
                        stationUuid = if (currentUuid.isNotEmpty()) currentUuid else java.util.UUID.randomUUID().toString(),
                        name = name,
                        url = url,
                        favicon = currentFavicon,
                        tags = currentTags,
                        country = currentCountry,
                        countryCode = currentCountryCode,
                        language = currentLanguage,
                        votes = currentVotes,
                        codec = currentCodec,
                        bitrate = currentBitrate
                    ))
                    
                    // Reset for next station
                    currentName = ""; currentUuid = ""; currentFavicon = ""; currentTags = ""
                    currentCountry = ""; currentCountryCode = ""; currentLanguage = ""
                    currentVotes = 0; currentCodec = ""; currentBitrate = 0
                }
            }
        }
        val homeGenres = homeGenresRaw.split(",").map { decodePlaylistField(it).trim() }.filter { it.isNotEmpty() }.toSet()
        val homeCountries = homeCountriesRaw.split(",").map { decodePlaylistField(it).trim() }.filter { it.isNotEmpty() }.toSet()
        val homeSettings = if (homeGenres.isNotEmpty() || homeCountries.isNotEmpty()) {
            PendingHomeSettings(homeGenres, homeCountries)
        } else null
        return ParsedBackup(importedStations, homeSettings)
    }

    private fun mergeImportedStations(importedStations: List<Station>) {
        val currentFavorites = _favorites.value.toMutableSet()
        val currentStationList = _favoriteStations.value.toMutableList()
        
        importedStations.forEach { station ->
            if (!currentFavorites.contains(station.stationUuid)) {
                currentFavorites.add(station.stationUuid)
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

    /**
     * Restores the Home-tab curation (visible genre/country sections) carried by a backup.
     * A [null] [settings] means the file held no home categories, in which case the current
     * curation is left untouched. With [replace] the imported sets overwrite the current
     * ones; otherwise they are merged (union).
     */
    private fun applyHomeSettings(settings: PendingHomeSettings?, replace: Boolean) {
        if (settings == null) return
        val newGenres = if (replace) settings.genres else _visibleGenres.value + settings.genres
        val newCountries = if (replace) settings.countries else _visibleCountries.value + settings.countries
        _visibleGenres.value = newGenres
        _visibleCountries.value = newCountries
        saveVisibleGenres(newGenres)
        saveVisibleCountries(newCountries)
        if (_selectedNavItem.value == NavigationItem.Home) {
            loadTopStations()
        }
    }

    private fun startPlaybackTimer() {
        viewModelScope.launch {
            while (true) {
                if (_isPlaying.value) {
                    _playbackTime.value = player?.currentPosition ?: 0L
                    val duration = player?.duration ?: 0L
                    // Live streams should not show a progress bar
                    val isLive = player?.isCurrentMediaItemLive ?: false
                    _playbackDuration.value = if (duration > 0 && !isLive) duration else 0L
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                _serverStats.value = repository.getStats()
            } catch (_: Exception) {
                // Optional server stats; a transient network failure must not crash the app.
            }
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
                // De-duplicated because the UI grids key their items by these values and
                // Compose crashes on duplicate keys.
                _tags.value = repository.getTags(limit = 500).distinctBy { it.name }
                _countries.value = repository.getCountries().distinctBy { it.iso_3166_1 to it.name }
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

    private fun loadRecentSearches(): List<String> {
        val json = prefs.getString("recent_searches_json", null) ?: return emptyList()
        return try {
            com.google.gson.Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveRecentSearches(searches: List<String>) {
        val json = com.google.gson.Gson().toJson(searches)
        prefs.edit().putString("recent_searches_json", json).apply()
    }

    fun addToRecentSearches(query: String) {
        if (query.length < 3) return
        val current = _recentSearches.value.toMutableList()
        
        current.removeAll { it.equals(query, ignoreCase = true) }
        current.add(0, query)
        
        if (current.size > 12) current.removeAt(current.size - 1)
        _recentSearches.value = current
        saveRecentSearches(current)
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        saveRecentSearches(emptyList())
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

    private fun loadVisibleCountries(): Set<String> {
        return prefs.getStringSet("visible_countries", emptySet()) ?: emptySet()
    }

    private fun saveVisibleCountries(countries: Set<String>) {
        prefs.edit().putStringSet("visible_countries", countries).apply()
    }

    private fun loadHideBroken(): Boolean {
        return prefs.getBoolean("hide_broken", true)
    }

    private fun saveHideBroken(hide: Boolean) {
        prefs.edit().putBoolean("hide_broken", hide).apply()
    }

    private fun loadAppTheme(): AppTheme {
        val name = prefs.getString("app_theme", AppTheme.ModernBlue.name) ?: AppTheme.ModernBlue.name
        return try { AppTheme.valueOf(name) } catch (e: Exception) { AppTheme.ModernBlue }
    }

    private fun loadAppLanguage(): AppLanguage {
        val lang = prefs.getString("app_language", AppLanguage.English.name) ?: AppLanguage.English.name
        return try { AppLanguage.valueOf(lang) } catch (e: Exception) { AppLanguage.English }
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("app_language", language.name).apply()
        applyLanguage(language)
    }

    private fun applyLanguage(language: AppLanguage) {
        val localeTag = when (language) {
            AppLanguage.English -> "en"
            AppLanguage.Russian -> "ru"
            AppLanguage.Ukrainian -> "uk"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    private fun loadDefaultCategory(): NavigationItem {
        val name = prefs.getString("default_category", NavigationItem.Home.name) ?: NavigationItem.Home.name
        return try { NavigationItem.valueOf(name) } catch (e: Exception) { NavigationItem.Home }
    }

    fun setAppTheme(theme: AppTheme) {
        _appTheme.value = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    fun setDefaultCategory(item: NavigationItem) {
        _defaultCategory.value = item
        prefs.edit().putString("default_category", item.name).apply()
    }

    fun setQuitConfirmationEnabled(enabled: Boolean) {
        _quitConfirmationEnabled.value = enabled
        prefs.edit().putBoolean("quit_confirmation", enabled).apply()
    }

    fun setAutoReconnectEnabled(enabled: Boolean) {
        _autoReconnectEnabled.value = enabled
        prefs.edit().putBoolean("auto_reconnect", enabled).apply()
        if (!enabled) {
            retryJob?.cancel()
            _infoMessage.value = null
        }
    }

    fun setExtraBufferingEnabled(enabled: Boolean) {
        _extraBufferingEnabled.value = enabled
        prefs.edit().putBoolean("extra_buffering", enabled).apply()
        reinitializePlayer()
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

    fun toggleMinTagFilter() {
        val newValue = !_minTagFilter.value
        _minTagFilter.value = newValue
        prefs.edit().putBoolean("min_tag_filter", newValue).apply()
    }

    fun setSettingsSubMenu(menu: String?) {
        _settingsSubMenu.value = menu
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun cancelRetry() {
        retryJob?.cancel()
        stationRetryCount = 0
        _error.value = null
        _infoMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun setSuccess(message: String) {
        _successMessage.value = message
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun setGenreSortMode(mode: GenreSortMode) {
        _genreSortMode.value = mode
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

    fun toggleCountryVisibility(countryName: String) {
        val current = _visibleCountries.value.toMutableSet()
        if (current.contains(countryName)) {
            current.remove(countryName)
        } else {
            current.add(countryName)
        }
        _visibleCountries.value = current
        saveVisibleCountries(current)
        if (_selectedNavItem.value == NavigationItem.Home) {
            loadTopStations()
        }
    }

    fun setTagSearchQuery(query: String) {
        _tagSearchQuery.value = query
    }

    private fun refreshCurrentContent() {
        when (_selectedNavItem.value) {
            NavigationItem.Home -> loadTopStations()
            NavigationItem.Popular -> loadPopularStations()
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

    fun selectNavigationItem(item: NavigationItem, force: Boolean = false) {
        val isNewItem = _selectedNavItem.value != item
        if (!isNewItem && !force && item != NavigationItem.Search) return

        beginContentRequest()
        _isLoading.value = false
        
        _selectedNavItem.value = item
        _error.value = null
        _selectedTag.value = null
        _selectedCountry.value = null
        _selectedSearchTag.value = null
        _settingsSubMenu.value = null
        
        if (isNewItem) {
            _allStations.value = emptyList()
            _stations.value = emptyList()
            _genreGroups.value = emptyList()
        }

        when (item) {
            NavigationItem.Home -> loadTopStations()
            NavigationItem.Popular -> loadPopularStations()
            NavigationItem.Recent -> {
                _allStations.value = _recentStations.value
                _stations.value = _recentStations.value // Don't apply bitrate filtering
                refreshRecentStations()
            }
            NavigationItem.Genres -> loadTags()
            NavigationItem.Countries -> loadCountries()
            NavigationItem.Favourites -> loadFavorites()
            NavigationItem.Search -> {
                _genreGroups.value = emptyList()
                _tagSearchGroups.value = emptyList()
                if (_searchQuery.value.length > 2) {
                    if (_searchMode.value == SearchMode.Tag) {
                        searchStationsByTag(_searchQuery.value)
                    } else {
                        searchStations(_searchQuery.value)
                    }
                } else {
                    _allStations.value = emptyList()
                    _stations.value = emptyList()
                }
                _searchFocusTrigger.value++
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
        
        when {
            _selectedTag.value != null -> selectTag(_selectedTag.value)
            _selectedCountry.value != null -> selectCountry(_selectedCountry.value)
            _selectedNavItem.value == NavigationItem.Popular -> loadPopularStations()
            _selectedNavItem.value == NavigationItem.Home -> loadTopStations()
            else -> applyFilters()
        }
    }

    private fun applyFilters() {
        val bitrates = _selectedBitrates.value
        val selectedSearchTag = _selectedSearchTag.value
        _stations.value = _allStations.value.filter { station ->
            (selectedSearchTag == null || stationHasTag(station, selectedSearchTag)) &&
                matchesBitrateFilter(station, bitrates)
        }

        _genreGroups.value = _genreGroups.value.map { group ->
            // Groups built from the cached tag/country counts carry no station list, so
            // their count must be kept as-is rather than recomputed over an empty list.
            group.copy(
                filteredCount = if (group.stations.isEmpty()) group.filteredCount
                                else group.stations.count { matchesBitrateFilter(it, bitrates) }
            )
        }

        _tagSearchGroups.value = _tagSearchGroups.value.map { group ->
            group.copy(filteredCount = group.stations.count { matchesBitrateFilter(it, bitrates) })
        }

        // Only auto-load more for sections that support server-side pagination.
        // Favourites and Recent are local lists — no API pagination needed.
        val supportsPagination = when (_selectedNavItem.value) {
            NavigationItem.Home, NavigationItem.Popular, NavigationItem.Genres,
            NavigationItem.Countries, NavigationItem.Search -> true
            else -> false
        }
        if (supportsPagination && !_isLoading.value && _stations.value.size < 100 && _hasMoreStations.value) {
            loadMoreStations(5)
        }
    }

    private fun matchesBitrateFilter(station: Station, bitrates: Set<BitrateFilter>): Boolean {
        if (bitrates.isEmpty()) return true
        val br = station.bitrate
        val isFlac = station.codec.equals("FLAC", ignoreCase = true) || 
                     station.tags.contains("flac", ignoreCase = true) ||
                     station.name.contains("flac", ignoreCase = true) ||
                     br >= 900
        
        return (bitrates.contains(BitrateFilter.Low) && br < 192 && !isFlac) ||
                (bitrates.contains(BitrateFilter.High) && br >= 192 && !isFlac) ||
                (bitrates.contains(BitrateFilter.FLAC) && isFlac)
    }

    private fun stationHasTag(station: Station, tagName: String): Boolean =
        station.tags.split(",").any { it.trim().equals(tagName, ignoreCase = true) }

    private fun loadPopularStations() {
        val requestId = beginContentRequest()
        viewModelScope.launch {
            _isLoading.value = true
            _stationOffset.value = 0
            _hasMoreStations.value = true
            try {
                val stations = repository.searchStations(limit = 100, offset = 0, hideBroken = _hideBrokenStations.value)
                if (!isCurrentContentRequest(requestId)) return@launch
                _hasMoreStations.value = stations.size >= 100
                updateStations(stations)
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Failed to load popular stations"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                    applyFilters()
                }
            }
        }
    }

    // Guards loadMoreStations re-entrancy on its own, synchronously, rather than relying
    // solely on _isLoading — that flag is also flipped by unrelated loads elsewhere, and
    // since it's only set inside the launched coroutine, two near-simultaneous callers
    // (e.g. applyFilters()'s auto-backfill firing while a chained page is already in
    // flight) could otherwise both pass the check before either sets it.
    private var loadingMoreStations = false

    fun loadMoreStations(remainingRetries: Int = 0) {
        if (!_hasMoreStations.value || _isLoading.value || loadingMoreStations) return
        loadingMoreStations = true
        val requestId = contentRequestId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newOffset = _stationOffset.value + 100
                val hideBroken = _hideBrokenStations.value
                val newStations = when (_selectedNavItem.value) {
                    NavigationItem.Popular -> {
                        repository.searchStations(limit = 100, offset = newOffset, hideBroken = hideBroken)
                    }
                    NavigationItem.Home, NavigationItem.Genres -> {
                        val tag = _selectedTag.value
                        if (tag != null) {
                            repository.searchStations(tag = tag.name, tagExact = true, limit = 100, offset = newOffset, hideBroken = hideBroken)
                        } else {
                            repository.searchStations(limit = 100, offset = newOffset, hideBroken = hideBroken)
                        }
                    }
                    NavigationItem.Countries -> {
                        val country = _selectedCountry.value
                        if (country != null) {
                            searchCountryStations(country.name, country.iso_3166_1, offset = newOffset, hideBroken = hideBroken)
                        } else emptyList()
                    }
                    NavigationItem.Search -> {
                        val selectedTag = _selectedSearchTag.value
                        val query = _searchQuery.value
                        if (selectedTag != null) {
                            repository.searchStations(tag = selectedTag, tagExact = true, limit = 100, offset = newOffset, hideBroken = hideBroken)
                        } else if (query.isNotEmpty()) {
                            if (_searchMode.value == SearchMode.Name) {
                                repository.searchStations(query = query, limit = 100, offset = newOffset, hideBroken = hideBroken)
                            } else {
                                repository.searchStations(tag = query, limit = 100, offset = newOffset, hideBroken = hideBroken)
                            }
                        } else emptyList()
                    }
                    else -> emptyList()
                }
                if (!isCurrentContentRequest(requestId)) return@launch
                _hasMoreStations.value = newStations.size >= 100
                _stationOffset.value = newOffset
                _allStations.value = (_allStations.value + newStations).distinctBy { it.stationUuid }
                applyFilters()
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Failed to load more stations"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                }
                loadingMoreStations = false
            }

            // Chain more pages if filtered count is still low
            if (isCurrentContentRequest(requestId) && remainingRetries > 0 && _stations.value.size < 100 && _hasMoreStations.value) {
                loadMoreStations(remainingRetries - 1)
            }
        }
    }

    private fun loadTopStations() {
        val requestId = beginContentRequest()
        viewModelScope.launch {
            _isLoading.value = true

            // Instantly update genre group counts from existing data before refreshing
            if (_genreGroups.value.isNotEmpty()) {
                applyFilters()
            }

            try {
                val selectedGenres = _visibleGenres.value
                val selectedCountries = _visibleCountries.value
                val hideBroken = _hideBrokenStations.value
                
                if (selectedGenres.isEmpty() && selectedCountries.isEmpty()) {
                    _genreGroups.value = emptyList()
                    _stationOffset.value = 0
                    val topStations = repository.getTopStations(hideBroken = hideBroken)
                    if (!isCurrentContentRequest(requestId)) return@launch
                    _hasMoreStations.value = topStations.size >= 100
                    if (_selectedBitrates.value.contains(BitrateFilter.FLAC)) {
                        val flacStations = repository.searchStations(tag = "flac", limit = 100, hideBroken = hideBroken)
                        if (!isCurrentContentRequest(requestId)) return@launch
                        updateStations((topStations + flacStations).distinctBy { it.stationUuid })
                    } else {
                        updateStations(topStations)
                    }
                } else {
                    val bitrates = _selectedBitrates.value

                    // Fast path: with no bitrate filter the Home tiles only need the cached
                    // tag/country counts (loaded at startup), so the screen can render
                    // instantly without one API request per selected category. Opening a
                    // tile fetches its station list on demand.
                    if (bitrates.isEmpty() && _tags.value.isNotEmpty() && _countries.value.isNotEmpty()) {
                        val groups = buildList {
                            selectedGenres.forEach { genre ->
                                val tag = _tags.value.find { it.name.equals(genre, ignoreCase = true) }
                                val total = tag?.stationcount ?: 0
                                add(GenreGroup(genre, emptyList(), total, total, isCountry = false))
                            }
                            selectedCountries.forEach { country ->
                                val c = _countries.value.find { it.name.equals(country, ignoreCase = true) }
                                val total = c?.stationcount ?: 0
                                add(GenreGroup(country, emptyList(), total, total, isCountry = true))
                            }
                        }
                        if (isCurrentContentRequest(requestId)) {
                            _genreGroups.value = groups
                        }
                        return@launch
                    }

                    // Network path (bitrate filters active, or tag/country caches not ready).
                    // Throttle concurrent requests and publish each group as it finishes so
                    // the Home screen fills in progressively instead of waiting for every
                    // category to complete.
                    //
                    // Tiles already on screen are kept (minus categories that are no longer
                    // selected) and each one is REPLACED in place when its fresh data arrives.
                    // Appending instead used to duplicate every tile (and crash the grid,
                    // which keys items by category name) whenever Home was reloaded, e.g.
                    // after toggling a bitrate filter.
                    _genreGroups.value = _genreGroups.value
                        .filter { g ->
                            if (g.isCountry) g.genreName in selectedCountries else g.genreName in selectedGenres
                        }
                        .distinctBy { it.isCountry to it.genreName }

                    // Each request catches its own failure. An exception escaping an async
                    // child would cancel this launch and reach the uncaught-exception handler
                    // (killing the app) even though await() sits inside try/catch below.
                    val throttle = Semaphore(maxHomeRequestsInFlight)
                    val deferred = selectedGenres.map { genre ->
                        async(Dispatchers.IO) {
                            try {
                                throttle.withPermit { fetchHomeGenreGroup(genre, bitrates, hideBroken) }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                null
                            }
                        }
                    } + selectedCountries.map { country ->
                        async(Dispatchers.IO) {
                            try {
                                throttle.withPermit { fetchHomeCountryGroup(country, bitrates, hideBroken) }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    var failedCount = 0
                    deferred.forEach { d ->
                        val group = d.await()
                        if (!isCurrentContentRequest(requestId)) return@launch
                        if (group == null) {
                            failedCount++
                            return@forEach
                        }
                        val current = _genreGroups.value
                        val index = current.indexOfFirst {
                            it.isCountry == group.isCountry && it.genreName == group.genreName
                        }
                        _genreGroups.value = if (index >= 0) {
                            current.toMutableList().also { it[index] = group }
                        } else {
                            current + group
                        }
                    }
                    if (failedCount > 0 && isCurrentContentRequest(requestId)) {
                        _error.value = "Failed to load $failedCount of ${deferred.size} categories"
                    }
                }
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Failed to load stations"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                    if (_genreGroups.value.isEmpty()) {
                        applyFilters()
                    }
                }
            }
        }
    }

    /**
     * Fetches stations for one Home genre category. Paginates only enough to reach a
     * meaningful filtered count (capped by [maxHomeGroupPages]) instead of scanning the
     * whole category, which is many API calls for large genres.
     */
    private suspend fun fetchHomeGenreGroup(
        genre: String,
        bitrates: Set<BitrateFilter>,
        hideBroken: Boolean
    ): GenreGroup {
        var offset = 0
        var allStations = repository.searchStations(tag = genre, tagExact = true, limit = 100, offset = offset, hideBroken = hideBroken)
        var filteredCount = allStations.count { matchesBitrateFilter(it, bitrates) }
        var pages = 1
        while (pages < maxHomeGroupPages && allStations.size >= 100 && filteredCount < 100) {
            offset += 100
            val moreStations = repository.searchStations(tag = genre, tagExact = true, limit = 100, offset = offset, hideBroken = hideBroken)
            if (moreStations.isEmpty()) break
            allStations = (allStations + moreStations).distinctBy { it.stationUuid }
            filteredCount = allStations.count { matchesBitrateFilter(it, bitrates) }
            pages++
        }
        val total = _tags.value.find { it.name.equals(genre, ignoreCase = true) }?.stationcount ?: allStations.size
        return GenreGroup(genre, allStations, total, filteredCount, isCountry = false)
    }

    /**
     * Stations for one country. radio-browser's `country=` parameter is a substring match
     * ("Niger" also returns Nigeria, "Sudan" also South Sudan), so filter by the ISO code
     * when it is known and fall back to an exact name match otherwise.
     */
    private suspend fun searchCountryStations(
        countryName: String,
        isoCode: String?,
        tag: String? = null,
        offset: Int = 0,
        hideBroken: Boolean
    ): List<Station> {
        val code = isoCode?.trim()?.takeIf { it.isNotEmpty() }
            ?: _countries.value.firstOrNull { it.name.equals(countryName, ignoreCase = true) }
                ?.iso_3166_1?.trim()?.takeIf { it.isNotEmpty() }
        return if (code != null) {
            repository.searchStations(countryCode = code, tag = tag, limit = 100, offset = offset, hideBroken = hideBroken)
        } else {
            repository.searchStations(country = countryName, countryExact = true, tag = tag, limit = 100, offset = offset, hideBroken = hideBroken)
        }
    }

    private suspend fun fetchHomeCountryGroup(
        country: String,
        bitrates: Set<BitrateFilter>,
        hideBroken: Boolean
    ): GenreGroup {
        var offset = 0
        var allStations = searchCountryStations(country, null, offset = offset, hideBroken = hideBroken)
        var filteredCount = allStations.count { matchesBitrateFilter(it, bitrates) }
        var pages = 1
        while (pages < maxHomeGroupPages && allStations.size >= 100 && filteredCount < 100) {
            offset += 100
            val moreStations = searchCountryStations(country, null, offset = offset, hideBroken = hideBroken)
            if (moreStations.isEmpty()) break
            allStations = (allStations + moreStations).distinctBy { it.stationUuid }
            filteredCount = allStations.count { matchesBitrateFilter(it, bitrates) }
            pages++
        }
        val total = _countries.value.find { it.name.equals(country, ignoreCase = true) }?.stationcount ?: allStations.size
        return GenreGroup(country, allStations, total, filteredCount, isCountry = true)
    }

    private fun loadTags() {
        val requestId = contentRequestId
        viewModelScope.launch {
            if (_tags.value.isEmpty()) {
                _isLoading.value = true
                try {
                    val tags = repository.getTags()
                    _tags.value = tags.distinctBy { it.name }
                } catch (e: Exception) {
                    if (isCurrentContentRequest(requestId)) {
                        _error.value = "Failed to load genres"
                    }
                } finally {
                    if (isCurrentContentRequest(requestId)) {
                        _isLoading.value = false
                    }
                }
            } else if (isCurrentContentRequest(requestId)) {
                _isLoading.value = false
            }
        }
    }

    private fun loadCountries() {
        val requestId = contentRequestId
        viewModelScope.launch {
            if (_countries.value.isEmpty()) {
                _isLoading.value = true
                try {
                    val countries = repository.getCountries()
                    _countries.value = countries.distinctBy { it.iso_3166_1 to it.name }
                } catch (e: Exception) {
                    if (isCurrentContentRequest(requestId)) {
                        _error.value = "Failed to load countries"
                    }
                } finally {
                    if (isCurrentContentRequest(requestId)) {
                        _isLoading.value = false
                    }
                }
            } else if (isCurrentContentRequest(requestId)) {
                _isLoading.value = false
            }
        }
    }

    private fun refreshFavoriteStations() {
        val uuids = _favorites.value.joinToString(",")
        if (uuids.isEmpty()) return
        viewModelScope.launch {
            try {
                val updatedStations = repository.getStationsByUuid(uuids)
                if (updatedStations.isNotEmpty()) {
                    val existingStations = _favoriteStations.value
                    val updatedUuids = updatedStations.map { it.stationUuid }.toSet()
                    val merged = existingStations.filter { it.stationUuid !in updatedUuids } + updatedStations
                    _favoriteStations.value = merged
                    saveFavoritesToPrefs(_favorites.value, merged)
                    if (_selectedNavItem.value == NavigationItem.Favourites) {
                        _stations.value = merged
                    }
                    _currentStation.value?.let { current ->
                        merged.find { it.stationUuid == current.stationUuid }?.let {
                            if (it.countryCode != current.countryCode) {
                                _currentStation.value = it
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun refreshRecentStations() {
        val uuids = _recentStations.value.map { it.stationUuid }.joinToString(",")
        if (uuids.isEmpty()) return
        viewModelScope.launch {
            try {
                val updatedStations = repository.getStationsByUuid(uuids)
                if (updatedStations.isNotEmpty()) {
                    val currentOrder = _recentStations.value.map { it.stationUuid }
                    val updatedList = currentOrder.mapNotNull { uuid ->
                        updatedStations.find { it.stationUuid == uuid }
                    }
                    _recentStations.value = updatedList
                    saveRecentsToPrefs(updatedList)
                    if (_selectedNavItem.value == NavigationItem.Recent) {
                        _allStations.value = updatedList
                        applyFilters()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadFavorites() {
        _stations.value = _favoriteStations.value
        _allStations.value = _favoriteStations.value // Keep in sync but don't filter
        refreshFavoriteStations()
    }

    /**
     * Whether a station is considered part of the favourites collection.
     *
     * Favourites are matched by:
     * 1. exact Radio Browser UUID (the normal case), or
     * 2. normalized stream URL, or
     * 3. case-insensitive whole-name match (never a partial / substring match).
     *
     * The URL/name fallbacks cover stations imported from external playlists: those carry
     * locally-generated UUIDs (and possibly older URLs), so they never equal the real
     * Radio Browser record returned by search - without the fallbacks their favourite
     * state would be invisible.
     */
    fun isFavorite(station: Station): Boolean {
        if (_favorites.value.contains(station.stationUuid)) return true
        val favouriteStations = _favoriteStations.value
        if (favouriteStations.isEmpty()) return false

        val stationUrl = normalizeStreamUrl(station.url)
        val stationName = station.name.trim().lowercase()
        if (stationUrl.isEmpty() && stationName.isEmpty()) return false

        return favouriteStations.any { fav ->
            if (fav.stationUuid == station.stationUuid) return@any true
            val favUrl = normalizeStreamUrl(fav.url)
            if (favUrl.isNotEmpty() && favUrl == stationUrl) return@any true
            val favName = fav.name.trim().lowercase()
            // Whole-name equality only. A substring test made a favourite called "Rock"
            // light up every station containing "rock", and toggling one of those removed
            // the real favourite instead of adding the new station.
            favName.isNotEmpty() && favName == stationName
        }
    }

    private fun normalizeStreamUrl(url: String): String = url.trim().trimEnd('/').lowercase()

    fun toggleFavorite(station: Station) {
        val currentFavorites = _favorites.value.toMutableSet()
        val currentStationList = _favoriteStations.value.toMutableList()

        if (isFavorite(station)) {
            // Prefer removing by exact UUID; for playlist-imported duplicates (different
            // UUID / URL / name), remove the matching stored favourite instead.
            if (currentFavorites.contains(station.stationUuid)) {
                currentFavorites.remove(station.stationUuid)
                currentStationList.removeAll { it.stationUuid == station.stationUuid }
            } else {
                val match = currentStationList.firstOrNull { favouriteMatches(station, it) }
                if (match != null) {
                    currentFavorites.remove(match.stationUuid)
                    currentStationList.remove(match)
                }
            }
        } else {
            currentFavorites.add(station.stationUuid)
            if (!currentStationList.any { it.stationUuid == station.stationUuid || normalizeStreamUrl(it.url) == normalizeStreamUrl(station.url) }) {
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

    /** Single-station comparison mirroring the fallback rules in [isFavorite]. */
    private fun favouriteMatches(candidate: Station, favourite: Station): Boolean {
        if (candidate.stationUuid == favourite.stationUuid) return true
        val candidateUrl = normalizeStreamUrl(candidate.url)
        val favouriteUrl = normalizeStreamUrl(favourite.url)
        if (candidateUrl.isNotEmpty() && candidateUrl == favouriteUrl) return true
        val candidateName = candidate.name.trim().lowercase()
        val favouriteName = favourite.name.trim().lowercase()
        return candidateName.isNotEmpty() && candidateName == favouriteName
    }

    fun exportFavoritesToM3u(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication() as Context
                val content = generateM3uContent()

                val wroteFile = withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    } != null
                }
                check(wroteFile) {
                    "Unable to open the selected destination"
                }
                _successMessage.value = "Favourites exported successfully"
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    /**
     * Writes the backup into the app cache and publishes it as a share request, so a file
     * manager that handles "Save to..." (`ACTION_SEND`) can store it wherever the user
     * wants. This is how Backup Data works on TVs without DocumentsUI (e.g. File Manager+).
     */
    fun prepareBackupForSharing(target: ComponentName? = null) {
        viewModelScope.launch {
            try {
                val fileName = getTimestampedBackupFileName()
                val context = getApplication<Application>()
                val uri = withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "backups")
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw java.io.IOException("Could not create the backup file")
                    }
                    // Only the newest backup needs to stay in the cache.
                    dir.listFiles()?.forEach { old -> if (old.name != fileName) old.delete() }
                    val file = File(dir, fileName)
                    file.writeText(generateM3uContent())
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${com.toxa.pureradio.BuildConfig.APPLICATION_ID}.fileprovider",
                        file
                    )
                }
                _backupShareRequest.value = BackupShareRequest(uri, fileName, target)
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    fun clearBackupShareRequest() {
        _backupShareRequest.value = null
    }

    /**
     * Backup fallback for devices that have no document picker at all (many Android TV
     * firmwares only ship the "you don't have an app that can do this" stub). Saves to
     * Downloads/PureRadio, which needs no storage permission on Android 10+ and is visible
     * from any file manager.
     */
    fun exportFavoritesToDownloads() {
        viewModelScope.launch {
            try {
                val fileName = getTimestampedBackupFileName()
                val content = generateM3uContent()
                val location = withContext(Dispatchers.IO) { writeBackupToDownloads(fileName, content) }
                _successMessage.value = "Backup saved to $location"
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    private fun writeBackupToDownloads(fileName: String, content: String): String {
        val context = getApplication<Application>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "audio/x-mpegurl")
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/PureRadio"
                )
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw java.io.IOException("Could not create the backup file")
            try {
                resolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
                    ?: throw java.io.IOException("Could not open the backup file")
                values.clear()
                values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            return "${Environment.DIRECTORY_DOWNLOADS}/PureRadio/$fileName"
        }
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "PureRadio"
        )
        if (!dir.exists() && !dir.mkdirs()) throw java.io.IOException("Could not create ${dir.path}")
        val file = File(dir, fileName)
        file.writeText(content)
        return file.absolutePath
    }

    fun importFavoritesFromM3u(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication() as Context
                val parsed = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        parsePlaylistLines(inputStream.bufferedReader().readLines())
                    } ?: ParsedBackup(emptyList(), null)
                }
                
                if (parsed.stations.isNotEmpty() || parsed.homeSettings != null) {
                    // Same Add / Replace dialog as the built-in file browser
                    // (see confirmRestore / cancelRestore).
                    _pendingImportStations.value = parsed.stations
                    _pendingHomeSettings.value = parsed.homeSettings
                } else {
                    _error.value = "No stations found in file"
                }
            } catch (e: Exception) {
                _error.value = "Import failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTag(tag: Tag?) {
        val isNewTag = _selectedTag.value != tag
        _selectedTag.value = tag
        if (tag != null) {
            _lastBrowsedCategory.value = tag.name
        }
        if (tag == null) {
            beginContentRequest()
            _isLoading.value = false
            return
        }
        if (isNewTag) {
            _allStations.value = emptyList()
            _stations.value = emptyList()
        }
        _stationOffset.value = 0
        _hasMoreStations.value = true
        val requestId = beginContentRequest()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isFlacFilter = _selectedBitrates.value.contains(BitrateFilter.FLAC)
                val stations = repository.searchStations(tag = tag.name, tagExact = true, limit = 100, hideBroken = _hideBrokenStations.value)
                if (!isCurrentContentRequest(requestId)) return@launch
                _hasMoreStations.value = stations.size >= 100
                if (isFlacFilter) {
                    val flacStations = repository.searchStations(tag = "flac", query = tag.name, limit = 100, hideBroken = _hideBrokenStations.value)
                    if (!isCurrentContentRequest(requestId)) return@launch
                    updateStations((stations + flacStations).distinctBy { it.stationUuid })
                } else {
                    updateStations(stations)
                }
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Failed to load stations for genre"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                    applyFilters()
                }
            }
        }
    }

    fun selectCountry(country: Country?) {
        val isNewCountry = _selectedCountry.value != country
        _selectedCountry.value = country
        if (country != null) {
            _lastBrowsedCategory.value = country.name
        }
        if (country == null) {
            beginContentRequest()
            _isLoading.value = false
            return
        }
        if (isNewCountry) {
            _allStations.value = emptyList()
            _stations.value = emptyList()
        }
        _stationOffset.value = 0
        _hasMoreStations.value = true
        val requestId = beginContentRequest()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isFlacFilter = _selectedBitrates.value.contains(BitrateFilter.FLAC)
                val stations = searchCountryStations(country.name, country.iso_3166_1, hideBroken = _hideBrokenStations.value)
                if (!isCurrentContentRequest(requestId)) return@launch
                _hasMoreStations.value = stations.size >= 100
                if (isFlacFilter) {
                    val flacStations = searchCountryStations(country.name, country.iso_3166_1, tag = "flac", hideBroken = _hideBrokenStations.value)
                    if (!isCurrentContentRequest(requestId)) return@launch
                    updateStations((stations + flacStations).distinctBy { it.stationUuid })
                } else {
                    updateStations(stations)
                }
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Failed to load stations for country"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                    applyFilters()
                }
            }
        }
    }

    fun toggleSearchMode() {
        _searchMode.value = when (_searchMode.value) {
            SearchMode.Name -> SearchMode.Tag
            SearchMode.Tag -> SearchMode.Name
        }
        _selectedSearchTag.value = null
        _tagSearchGroups.value = emptyList()
        if (_searchQuery.value.length > 2) {
            onSearchQueryChange(_searchQuery.value)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        val requestId = beginContentRequest()
        clearSearchResults()
        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                delay(400) // Debounce
                if (!isCurrentContentRequest(requestId) || _searchQuery.value != query) return@launch
                when (_searchMode.value) {
                    SearchMode.Name -> searchStations(query)
                    SearchMode.Tag -> searchStationsByTag(query)
                }
            }
        } else {
            _isLoading.value = false
        }
    }

    fun selectSearchTag(tagName: String?) {
        _selectedSearchTag.value = tagName
        if (tagName != null) {
            _lastBrowsedCategory.value = tagName
        }
        val requestId = beginContentRequest()
        if (tagName == null) {
            _isLoading.value = false
            applyFilters()
        } else {
            val hideBroken = _hideBrokenStations.value
            _stationOffset.value = 0
            _hasMoreStations.value = true
            
            // First show what we have locally
            val localFiltered = _allStations.value.filter { station ->
                stationHasTag(station, tagName) && matchesBitrateFilter(station, _selectedBitrates.value)
            }
            _stations.value = localFiltered
            
            // Then fetch comprehensive list for this specific tag
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val remoteStations = repository.searchStations(tag = tagName, tagExact = true, limit = 100, hideBroken = hideBroken)
                    if (!isCurrentContentRequest(requestId)) return@launch
                    _hasMoreStations.value = remoteStations.size >= 100
                    val combined = (_allStations.value + remoteStations).distinctBy { it.stationUuid }
                    _allStations.value = combined
                    
                    val filtered = combined.filter { station ->
                        stationHasTag(station, tagName) && matchesBitrateFilter(station, _selectedBitrates.value)
                    }
                    _stations.value = filtered
                } catch (e: Exception) {
                    // Fallback to local if remote fails
                } finally {
                    if (isCurrentContentRequest(requestId)) {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private val _searchFocusTrigger = MutableStateFlow(0)
    val searchFocusTrigger: StateFlow<Int> = _searchFocusTrigger

    fun clearSearch() {
        searchJob?.cancel()
        beginContentRequest()
        _isLoading.value = false
        _searchQuery.value = ""
        clearSearchResults()
        _searchFocusTrigger.value++
    }

    private fun clearSearchResults() {
        _allStations.value = emptyList()
        _stations.value = emptyList()
        _tagSearchGroups.value = emptyList()
        _selectedSearchTag.value = null
        _stationOffset.value = 0
        _hasMoreStations.value = false
    }

    private fun searchStationsByTag(tagQuery: String) {
        val requestId = beginContentRequest()
        _isLoading.value = true
        _error.value = null
        _selectedSearchTag.value = null
        _stationOffset.value = 0
        _hasMoreStations.value = true
        
        viewModelScope.launch {
            try {
                val hideBroken = _hideBrokenStations.value
                var offset = 0
                var allResults = repository.searchStations(tag = tagQuery, limit = 100, offset = offset, hideBroken = hideBroken)
                var retries = 0
                while (retries < 5 && allResults.size >= 100) {
                    offset += 100
                    val more = repository.searchStations(tag = tagQuery, limit = 100, offset = offset, hideBroken = hideBroken)
                    if (more.isEmpty()) break
                    allResults = (allResults + more).distinctBy { it.stationUuid }
                    retries++
                }

                if (!isCurrentContentRequest(requestId) || _searchQuery.value != tagQuery) return@launch
                
                val tagMap = mutableMapOf<String, MutableList<Station>>()
                allResults.forEach { station ->
                    station.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        tagMap.getOrPut(tag) { mutableListOf() }.add(station)
                    }
                }
                
                _tagSearchGroups.value = tagMap.entries
                    .filter { it.key.contains(tagQuery, ignoreCase = true) }
                    .sortedByDescending { it.value.size }
                    .map { (tag, stations) ->
                        GenreGroup(
                            genreName = tag,
                            stations = stations,
                            totalStations = stations.size,
                            filteredCount = stations.size
                        )
                    }
                
                _hasMoreStations.value = false // We already fetched many
                updateStations(allResults)
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Tag search failed"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun playStation(station: Station, resetErrors: Boolean = true) {
        if (resetErrors) {
            consecutiveErrors = 0
            stationRetryCount = 0
            _infoMessage.value = null
        }
        if (station.stationUuid != _currentStation.value?.stationUuid) {
            stationRetryCount = 0
            // Remember which list entry was chosen, even if a better stream of it ends up
            // playing (that stream has its own UUID and isn't in the list): Next/Previous
            // and the "now playing" highlight on the tiles work from this.
            _playingFromUuid.value = station.stationUuid
        }
        retryJob?.cancel()
        streamLookupJob?.cancel()
        _isLoading.value = false
        var finalStation = station
        if (finalStation.countryCode.isNullOrEmpty()) {
            _allStations.value.find { it.stationUuid == station.stationUuid && !it.countryCode.isNullOrEmpty() }?.let {
                finalStation = it
            }
        }
        
        if (_selectedNavItem.value == NavigationItem.Search && _searchQuery.value.length > 2) {
            addToRecentSearches(_searchQuery.value)
        }

        // "Best Stream Quality": for a station the user picked (not for reconnects or the
        // automatic skip after errors), first look for a better stream of the same station.
        // The lookup is capped at a couple of seconds so a slow API never delays playback much.
        if (resetErrors && _bestStreamEnabled.value) {
            val requested = finalStation
            player?.stop()
            _currentStation.value = requested
            _error.value = null
            _mediaMetadata.value = null
            _audioFormat.value = null
            streamLookupJob = viewModelScope.launch {
                val better = try {
                    withTimeoutOrNull(2_500) { findBetterStream(requested) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
                if (better != null) {
                    _successMessage.value = getApplication<Application>().getString(
                        com.toxa.pureradio.R.string.stream_upgraded_notice,
                        streamQualityText(better)
                    )
                }
                startPlayback(better ?: requested)
            }
        } else {
            startPlayback(finalStation)
        }
    }

    private fun startPlayback(finalStation: Station) {
        addToRecent(finalStation)
        _currentStation.value = finalStation
        _error.value = null
        _mediaMetadata.value = null
        _audioFormat.value = null
        // No explicit startForegroundService() here: PlaybackService registers its session
        // with Media3, which starts the service in the foreground with the standard media
        // notification as soon as playback begins (and handles the Android 12+ background
        // start restrictions itself).
        player?.let {
            it.stop()
            it.clearMediaItems()

            val mediaItemBuilder = MediaItem.Builder()
                .setUri(finalStation.url)
                .setMediaId(finalStation.stationUuid)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(finalStation.name)
                        .build()
                )

            if (finalStation.url.contains("m3u8", ignoreCase = true) ||
                finalStation.codec.equals("hls", ignoreCase = true)) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
            }

            it.setMediaItem(mediaItemBuilder.build())
            it.prepare()
            it.play()
            _isPlaying.value = true
        }
    }

    // ---------- Best stream selection ----------

    /**
     * radio-browser often lists one station several times — one entry per stream (e.g. an
     * MP3 128k and an AAC 320k). Returns a clearly better stream of the SAME station, or null.
     *
     * "Same station" = same name AND same homepage. Many unrelated stations share a name
     * ("Radio 1"), so without a homepage on both sides nothing is swapped.
     */
    private suspend fun findBetterStream(station: Station): Station? {
        val homepage = normalizeHomepage(station.homepage) ?: return null
        val name = station.name.trim()
        if (name.isEmpty() || station.bitrate <= 0 || isLossless(station)) return null
        val candidates = repository.searchStations(
            query = name,
            nameExact = true,
            limit = 50,
            hideBroken = true
        )
        return candidates
            .asSequence()
            .filter { it.stationUuid != station.stationUuid }
            .filter { it.name.trim().equals(name, ignoreCase = true) }
            .filter { normalizeHomepage(it.homepage) == homepage }
            .filter { it.url.isNotBlank() && !isVideoCodec(it) }
            .maxByOrNull { streamQualityScore(it) }
            ?.takeIf { isClearlyBetter(it, station) }
            ?.let { better ->
                // Keep the country code if only the original entry had it (for the flag).
                if (better.countryCode.isNullOrEmpty()) better.copy(countryCode = station.countryCode) else better
            }
    }

    private fun normalizeHomepage(url: String?): String? =
        url?.trim()?.lowercase()
            ?.removePrefix("https://")?.removePrefix("http://")?.removePrefix("www.")
            ?.trimEnd('/')
            ?.takeIf { it.isNotEmpty() }

    private fun isLossless(station: Station): Boolean =
        station.codec.orEmpty().contains("FLAC", ignoreCase = true)

    private fun isVideoCodec(station: Station): Boolean {
        val codec = station.codec.orEmpty().uppercase()
        return codec.contains("H.26") || codec.contains("VIDEO") || codec.contains("VP9") || codec.contains("AV1")
    }

    /**
     * Rough perceptual quality: lossless beats everything; otherwise bitrate, with AAC/Opus/
     * Vorbis credited ~30% over MP3 at the same bitrate (they are more efficient codecs).
     */
    private fun streamQualityScore(station: Station): Double {
        if (isLossless(station)) return 100_000.0 + station.bitrate
        if (station.bitrate <= 0) return 0.0
        val codec = station.codec.orEmpty().uppercase()
        val efficiency = if (codec.contains("AAC") || codec.contains("OPUS") ||
            codec.contains("OGG") || codec.contains("VORBIS")) 1.3 else 1.0
        return station.bitrate * efficiency
    }

    /** Only switch for a real improvement (≥20% better), never to an unknown-bitrate stream. */
    private fun isClearlyBetter(candidate: Station, current: Station): Boolean {
        if (isLossless(candidate) && !isLossless(current)) return true
        if (candidate.bitrate <= 0) return false
        return streamQualityScore(candidate) >= streamQualityScore(current) * 1.2
    }

    private fun streamQualityText(station: Station): String {
        val codec = station.codec.orEmpty().trim().uppercase().takeIf { it.isNotEmpty() && it != "UNKNOWN" }
        val bitrate = station.bitrate.takeIf { it > 0 }?.let { "${it}k" }
        return listOfNotNull(codec, bitrate).joinToString(" · ")
    }

    fun setBestStreamEnabled(enabled: Boolean) {
        _bestStreamEnabled.value = enabled
        prefs.edit().putBoolean("best_stream" /* = PlaybackService.PREF_BEST_STREAM */, enabled).apply()
        // Applies immediately to the current player (no rebuild needed).
        player?.let {
            it.trackSelectionParameters = it.trackSelectionParameters.buildUpon()
                .setForceHighestSupportedBitrate(enabled)
                .build()
        }
    }

    /** True for the entry that is playing: either its own stream or a better stream of it. */
    private fun isNowPlaying(station: Station, current: Station): Boolean =
        station.stationUuid == current.stationUuid || station.stationUuid == _playingFromUuid.value

    fun playNext(isAuto: Boolean = false) {
        val current = _currentStation.value ?: return
        
        // Use the primary filtered list if the current station is in it
        val activeList = _stations.value
        val indexInActive = activeList.indexOfFirst { isNowPlaying(it, current) }
        
        val list = if (indexInActive != -1) {
            activeList
        } else if (_selectedNavItem.value == NavigationItem.Home && 
                       _visibleGenres.value.isNotEmpty() && 
                       _selectedTag.value == null && 
                       _selectedCountry.value == null) {
            val bitrates = _selectedBitrates.value
            _genreGroups.value.flatMap { group ->
                group.stations.filter { matchesBitrateFilter(it, bitrates) }
            }
        } else {
            _stations.value
        }
        
        if (list.isEmpty()) return

        // If there's only one station and it failed, stop to avoid infinite loop
        if (list.size == 1 && isNowPlaying(list[0], current)) {
            _error.value = "Station unavailable"
            stopPlayback()
            return
        }

        val index = list.indexOfFirst { isNowPlaying(it, current) }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            playStation(list[nextIndex], !isAuto)
        }
    }

    fun playPrevious() {
        val current = _currentStation.value ?: return
        
        val activeList = _stations.value
        val indexInActive = activeList.indexOfFirst { isNowPlaying(it, current) }
        
        val list = if (indexInActive != -1) {
            activeList
        } else if (_selectedNavItem.value == NavigationItem.Home && 
                       _visibleGenres.value.isNotEmpty() && 
                       _selectedTag.value == null && 
                       _selectedCountry.value == null) {
            val bitrates = _selectedBitrates.value
            _genreGroups.value.flatMap { group ->
                group.stations.filter { matchesBitrateFilter(it, bitrates) }
            }
        } else {
            _stations.value
        }

        if (list.isEmpty()) return
        val index = list.indexOfFirst { isNowPlaying(it, current) }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            playStation(list[prevIndex])
        }
    }

    fun togglePlayPause() {
        retryJob?.cancel()
        _infoMessage.value = null
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
        retryJob?.cancel()
        retryResetJob?.cancel()
        streamLookupJob?.cancel()
        _playingFromUuid.value = null
        player?.stop()
        // Clearing the item also makes Media3 remove the media notification.
        player?.clearMediaItems()
        _isPlaying.value = false
        _currentStation.value = null
        _infoMessage.value = null
        // Media3 already dropped the service out of the foreground when the player stopped;
        // this clears its "started" state too. The service (and player) stay alive as long
        // as we're still bound, so the next playStation() call is instant.
        getApplication<Application>().stopService(Intent(getApplication(), PlaybackService::class.java))
    }

    fun searchStations(query: String) {
        if (query.isEmpty()) return
        val requestId = beginContentRequest()
        _isLoading.value = true
        _error.value = null
        _stationOffset.value = 0
        _hasMoreStations.value = true
        
        viewModelScope.launch {
            try {
                val results = repository.searchStations(query = query, hideBroken = _hideBrokenStations.value)
                
                val finalResults = if (_selectedBitrates.value.contains(BitrateFilter.FLAC)) {
                    val flacResults = repository.searchStations(query = query, tag = "flac", hideBroken = _hideBrokenStations.value)
                    (results + flacResults).distinctBy { it.stationUuid }
                } else {
                    results
                }

                if (!isCurrentContentRequest(requestId) || _searchQuery.value != query) return@launch
                
                _hasMoreStations.value = finalResults.size >= 100
                updateStations(finalResults)
            } catch (e: Exception) {
                if (isCurrentContentRequest(requestId)) {
                    _error.value = "Search failed"
                }
            } finally {
                if (isCurrentContentRequest(requestId)) {
                    _isLoading.value = false
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        retryJob?.cancel()
        retryResetJob?.cancel()
        player?.removeListener(playerListener)
        if (serviceBound) {
            getApplication<Application>().unbindService(playbackServiceConnection)
            serviceBound = false
        }
        // Deliberately not releasing/stopping the player here: the service owns its
        // lifecycle now, so playback keeps running if this ViewModel is only being
        // cleared because its Activity is being recreated or backgrounded. Explicit
        // "stop the app" flows (Exit, the PiP stop action) already call stopPlayback().
        player = null
        playbackService = null
    }

    /**
     * Start-up work. Deliberately the LAST declaration in the class: every property above is
     * initialized by the time this runs, so nothing called from here can see a null field.
     */
    init {
        applyLanguage(_appLanguage.value)
        loadTags()
        loadCountries()
        loadStats()
        selectNavigationItem(_defaultCategory.value, force = true)
        startPlaybackTimer()
        startScreensaverTimer()
        checkAutoUpdate()
        refreshFavoriteStations()
        refreshRecentStations()
        viewModelScope.launch {
            delay(500)
            _isInitialized.value = true
        }
        initializePlayer()
    }
}
