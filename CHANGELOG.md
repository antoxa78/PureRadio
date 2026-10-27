# Changelog

All notable changes to this project will be documented in this file.

## [1.4.2] - 2026-10-27

### Added
- **OLED themes**: OLED Crimson, OLED Emerald, OLED Magenta and OLED Amber — true black backgrounds with bright accents, so OLED pixels stay off and the contrast is much higher.

### Changed
- **Blue Neon** is now electric cyan on true black instead of looking almost identical to Modern Blue.

## [1.4.1] - 2026-10-27

### Fixed
- **Restore Data froze the app on some Android TV devices**: the system picker was opened with `ACTION_GET_CONTENT`, which on TVs without DocumentsUI only offers the photo picker; that picker tries to hand off to the missing DocumentsUI and lands back on itself in an endless activity loop. Restore now opens `ACTION_OPEN_DOCUMENT` directly and falls back to the built-in file browser when no document picker is installed.
- **File Manager+ is used directly when installed**, both for Restore (its document picker) and Backup (its “Save to…” folder browser), instead of the TV-unfriendly system DocumentsUI. Other pickers are still used when File Manager+ isn't present.
- **Backup Data showed "You don't have an app that can do this" on those TVs**: their only `ACTION_CREATE_DOCUMENT` handler is the TV framework's "missing app" stub. The stub is now recognised; the backup is handed to a file manager's "Save to..." share target (e.g. File Manager+) so any folder can be used, with `Downloads/PureRadio` as the fallback when no such app exists (no storage permission needed on Android 10+).

## [1.4.0] - 2026-10-26

### Changed (UI)
- **Empty & loading states**: Favourites, Recent, searches with no results and over-filtered lists now show a clear message with a hint instead of a blank screen; empty lists that are still loading show a spinner.
- **Non-blocking messages**: Errors and confirmations appear as a small card at the bottom (above the player bar) instead of dimming and hiding the whole screen, so you keep your place in the grid.
- **Page header**: The station count is shown next to the category name in a muted style (also on Favourites and Recent) instead of being duplicated.
- **Station tiles**: Codec/bitrate chip (e.g. `MP3 · 128k`, lossless highlighted); the playing station keeps an outline and a larger "now playing" badge.
- **Player bar**: Pulsing LIVE tag for live streams, accent line along the top, timer switches to h:mm:ss after an hour, favourite heart uses the same red as tile badges.
- **Settings**: Grouped into General / Playback / Station Database / Data Management sections; a single check mark marks the selected option in every picker; themes show a colour preview; removed the duplicated "Settings" heading.
- **Readability**: Accent text and icons fall back to a lighter tone in the Modern Blue / Blue Neon themes, where the deep blue was hard to read on dark backgrounds; the selected drawer entry is no longer dark-blue-on-dark-blue.
- **Navigation icons**: Popular, Genres and Countries use more fitting icons.
- **Search**: Localized NAME/TAG mode button with icons, history icons on recent searches and a "Clear" button.

### Added
- **Best Stream Quality** (Settings › Playback, on by default): when a station is listed on radio-browser with several streams, the best one (same name and homepage, ≥20% better, lossless preferred) is played automatically, with a short notice; adaptive HLS streams always use their top bitrate.

### Changed
- The "Audio Passthrough (Bit-Perfect)" setting is now called **High-Resolution Output** and describes what it really does (32-bit float output so 24-bit streams aren't cut to 16-bit; not bit-perfect). Its no-op extension-decoder option was removed.

### Fixed
- **Back navigation**: Back on a main screen opens the main menu; Back again asks to exit. Focus no longer gets lost after re-selecting the current section or answering "No" in the exit dialog; Back during the splash is ignored.
- **Crash on launch** when the start-up category was set to Search (ViewModel init order).
- **Crash on Home** when any single category request failed (timeout, rate limit, offline) while loading curated categories; failed tiles are now skipped with a short notice.
- **Duplicated Home tiles / crash** after toggling a bitrate filter or Hide-broken on Home; tiles are now refreshed in place. Grids also de-duplicate genres, countries and stations so duplicate keys can't crash them.
- **Media notification & lock-screen controls**: the playback session is now registered with Media3, which shows the standard media notification and manages the foreground service; the permanent "Connecting…" notification is gone.
- **Reopening the app during background playback** now shows the station that is playing (Now Playing bar, play state, metadata).
- **Backup / Restore** use the system file picker again (no storage permission needed); the built-in browser is only a fallback. No more redirect to "All files access" on every launch / PiP return. Android 9/10 now request write permission for the fallback browser. Restoring from the system picker shows the Add / Replace dialog.
- **Language switch on Android 12L and lower**: the activity is now an AppCompatActivity and the chosen language is stored by AppCompat.
- **Favourites**: a favourite no longer marks (or gets removed by) other stations whose names merely contain its name.
- **Countries / Genres**: stations are filtered by country code (or exact country name) and exact genre tag — "Niger" no longer lists Nigerian stations.
- **Picture-in-Picture** now enters on Android 9–13 too, and auto-enter is only armed while something is playing.
- Remaining hard-coded English text in the player bar, screensaver status line, search mode button and database update interval picker is now translated (RU/UK).

## [1.3.12] - 2026-10-03

### Added
- **Unified Backup & Restore**: "Backup Data" / "Restore Data" now exports both your favourite stations and your home-screen categories (genres/countries) into a single playlist file using the built-in file manager.
  - Restoring offers merge or replace; old favourites-only backup files remain fully compatible.
  - Backup files are standard M3U, so other players can still read your station list.

### Changed
- Backup files are now named `pureradio backup <date>.m3u`.

## [1.3.11] - 2026-09-09

### Changed
- Code cleanup and UI improvements across MainActivity, ViewModel, and repository.
- Updated translations (Russian/Ukrainian) and string resources.
- Added foreground service and network security configuration.

## [1.3.10] - 2026-08-14

### Fixed
- **Async Loading**: Prevented stale station and search responses from replacing newer content.
- **Search and Filtering**: Fixed short-query results, exact genre matching, selected-tag filtering, and pagination.
- **TV Navigation**: Back now closes the navigation drawer instead of exiting the app.
- **Reconnect UX**: Canceled reconnect notices are cleared correctly.
- **Playlist I/O**: Moved import and export file operations off the main thread.
- **Broadcast Security**: Scoped the PiP stop action to the application and declared the receiver as not exported.

### Security
- Release builds no longer use the shared Android debug signing key.

## [1.3.9] - 2026-07-21

### Improved
- **Reconnection UX**: Connection lost messages are now displayed as non-disruptive information messages at the bottom of the screen.
- **UI Stability**: Fixed an issue where the station selector would move or reset when reconnection attempts were in progress.

## [1.3.8] - 2026-07-20

### Fixed
- **TV Branding**: Fixed issue where older icons were displayed on Nvidia Shield (Leanback launcher).
- **Icon Quality**: Refined vector icons with high-quality gradients and better symmetry.
- **Adaptive Icons**: Updated launcher background with a premium radial gradient.

## [1.3.7] - 2026-07-20

### Added
- **MediaSession Integration**: Improved system-level media integration and player stability.
- **TV Stability**: Added screen wake lock to prevent the device from sleeping during playback.

### Fixed
- **Playback State Sync**: Fixed potential UI desync issues by driving the playback state directly from player events.
- **Error Handling**: Improved error transparency for network failures and search operations.
- **Exit Logic**: Standardized application closure behavior using standard Activity lifecycle.

## [1.3.6] - 2026-07-17

### Added
- **New Branding**: Modernized application icon and splash screen.
- **Refreshed Visuals**: Updated color palette to a dark blue/cyan theme across the app.

## [1.2.2] - 2026-05-29

### Changed
- Placeholder for v1.2.2 changes.

## [1.2.0] - 2025-01-31

### Added
- **Popular Stations**: New dedicated menu section for trending stations based on community votes.
- **Audio Passthrough (Hi-Res)**: Experimental mode to bypass Android's 48kHz resampler, featuring floating-point PCM output and renderer optimization (specifically for Nvidia Shield).
- **Anti-Burn-In Screensaver**: The "Station Info" screensaver now bounces across the screen to protect OLED/Plasma panels.
- **Live Stats in Screensaver**: Added real-time playback duration and an intelligent waveform that stops when audio is paused.
- **Vote Counts**: Station cards now display the number of community votes.

### Changed
- **Now Playing Bar**: Relocated playback timer to the center controls and improved layout for wider country names.
- **Drawer Layout**: Navigation drawer now uses a scrollable `LazyColumn` to prevent overlapping on smaller screens or long lists.
- **Error Handling**: Implemented automatic "Skip to Next" when a station URL is unplayable.

### Fixed
- Fixed critical layout issues where the navigation drawer would distort screen titles.
- Fixed missing country icons in Favourites and Recent lists by adding a background metadata refresh.
- Standardized all application icons and banners to fix legacy "Old Icon" display issues on newer Google TV boxes.

## [1.1.0] - 2025-01-30
- Added country flags and bitrate info near station names.
- Replaced VU meters with a modern center-weighted Waveform Analyzer.
- Added adaptive icon support.

## [1.0.0] - 2025-01-20
- Initial release with basic station browsing, playback, and search.
