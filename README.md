# Color Merge

A fully offline single-player Android puzzle game built with Kotlin and Jetpack Compose.

## Gameplay
- 6x6 board
- Tap a tile, then an adjacent tile with the same value to merge
- Merges create progressively higher-value tiles
- Score and locally saved best score
- Game-over detection
- Restart/new game
- Help/tutorial screen
- Mute/unmute sound effects
- No account and no internet required for gameplay

## Architecture
A deliberately small single-activity Compose app is used to keep the project easy to maintain. `GameState` owns the board and game rules, Compose renders the screens, and Android `SharedPreferences` stores best score and mute settings locally. No third-party game engine or remote service is required.

## Build
Open this repository in Android Studio (Ladybug or newer), let Gradle sync, then select the `app` configuration and run it on an Android 7.0+ device/emulator.

For a debug APK, use **Build > Build APK(s)**. The generated APK is under `app/build/outputs/apk/debug/`.

## Future-ready
The project has a simple application structure so an ad SDK can be added later without making ads part of the current gameplay.
