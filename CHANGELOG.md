# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

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

### Fixed
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
