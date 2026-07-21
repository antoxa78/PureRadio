# Changelog

All notable changes to this project will be documented in this file.

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
