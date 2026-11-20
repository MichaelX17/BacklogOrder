# BacklogOrder

**BacklogOrder** is a modern, Cyberpunk/HUD-styled Android application built with **Kotlin** and **Jetpack Compose**. It provides video game enthusiasts with an intuitive, powerful interface to track, rank, organize, rate, and discover games across custom backlog lists, status pipelines, and franchises.

---

## Features

### RAWG Game Search & Integration
- **Live Search**: Query millions of games directly via the RAWG API with real-time search debouncing.
- **Resilient Parsing**: Robust JSON handling (`kotlinx.serialization`) tolerating missing/incomplete API fields seamlessly.
- **Rating Fallback System**: Automatically converts 0–5 RAWG ratings into 0–100 score equivalents when official Metacritic scores are missing.
- **Multi-List Quick Add**: Assign games to multiple custom backlog lists simultaneously via a multi-selection dialog or create new lists on the fly.

### Smart Recommendation Engine & Dashboard
- **Value-for-Time Calculator**: Dynamic score formula (`Score = Metacritic / PlaytimeHours`) highlighting high-quality, time-efficient games.
- **Smart Recommendations**: Evaluates playing and backlog status candidates to surface the best next game to play.
- **Recommendation Controls**: Cycle through recommended candidates with skip and reset capabilities.
- **Library Analytics**: Real-time stats dashboard displaying total games, completed titles, total playtime hours, and backlog counts.

### Interactive 1:1 Aspect-Ratio Cover Crop Tool & Manual Game Entry
- **Manual Game Creation**: Add non-RAWG games with custom titles, release dates, genres, platforms, metacritic scores, ratings, and playtime.
- **Interactive Cropper**: Custom 1:1 square image crop dialog with touch drag, pinch-to-zoom, and boundary clamping to eliminate empty black borders.
- **Local Cover Management**: Automatic local caching and downloader utilities for offline cover artwork.

### Multi-List & Deduplicated All-Games View
- **Custom Lists**: Create, rename, and manage custom backlog lists (e.g., *Backlog*, *Currently Playing*, *Completed*, *Favorites*).
- **Consolidated Library**: View all saved games in a deduplicated "All Games" overview with list membership badges, status chips (`BACKLOG`, `PLAYING`, `COMPLETED`, `DROPPED`), and score badges.
- **Filter & Sort**: Instant multi-attribute filtering (by name, status, rating) and sorting (by title, score, release date, playtime).

### Intelligent Franchise Hub & Auto-Sorter
- **Automatic Franchise Extractor**: Extractor algorithm parses franchise names from game titles (handles colons, dashes, Roman numerals, trailing numbers, and subtitle keywords).
- **Franchise Autocomplete**: Instant search suggestions for franchise names when editing or adding games.
- **Franchise Grouping**: Group games into franchises (*"The Witcher"*, *"Final Fantasy"*, *"God of War"*) with custom sequencing (`#1`, `#2`) and manual overrides in list detail views.

### Data Backup, Export & Merge System
- **JSON Backup Export**: Export full database snapshots adhering to Schema V1 with SHA-256 content hashing for integrity verification.
- **Native Android Sharing**: Share export archives directly via system intent (`FileProvider`).
- **CSV Import Engine**: Import game collections from CSV files (e.g. Steam / custom spreadsheet exports).
- **Interactive Import Engine**: Pre-commit merge preview dialog with conflict detection (Skip, Overwrite, or Merge strategies).

### Customizable Cyberpunk HUD Design System
- **5 Theme Presets**: Cyberpunk Violet (Default), Neon Cyan, Crimson Warning, Emerald Matrix, and Gold Executive.
- **Custom Accent Picker**: Personalize the HUD primary accent color using custom Hex codes.
- **Dynamic Dark & Light Modes**: Full support for both dark and light UI modes with custom HUD palette mapping.
- **Sci-Fi Visuals**: Bevel frames, hex score chips, sci-fi borders, and animated status badges.

### Security & Onboarding
- **Encrypted API Key Storage**: Secure persistence of RAWG API keys using `EncryptedSharedPreferences` via AndroidX Security Crypto.
- **First-Run Onboarding**: Guided setup wizard for API key validation with instant testing and optional skip mode.

---

## Architecture & Project Structure

The project follows **Clean Architecture** and **MVVM** principles:

```
app/src/main/java/com/backlogorder/
├── data/              # Room database, Entities, DAOs, Repositories, RAWG API client & Encrypted Storage
├── di/                # Dependency Injection (Hilt / AppContainer, AppModule, AppDispatchers)
├── domain/            # Pure Kotlin Domain Models, Score Calculator, Recommendation Engine, Export/Import & Merge Logic
├── ui/                # Jetpack Compose Screens, ViewModels, HUD Design System & Navigation Graph
└── util/              # Utilities (CoverImageDownloader, CsvParser, FranchiseExtractor)
```

---

## Technologies & Libraries

| Domain / Layer | Technology |
| :--- | :--- |
| **Target / Compile SDK** | Android 15 (SDK 37) / Min SDK 26 |
| **Language** | [Kotlin](https://kotlinlang.org/) 2.2+ |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://developer.android.com/jetpack/compose/material3) |
| **Design System** | Custom Cyberpunk HUD Theme & Components |
| **Architecture** | MVVM / Clean Architecture with `StateFlow` & `ViewModel` |
| **Dependency Injection** | [Hilt](https://dagger.dev/hilt/) (`hilt-android`) |
| **Database** | [Room Database](https://developer.android.com/training/data-storage/room) 2.8+ with KSP Schema Exports |
| **Security** | AndroidX Security Crypto (`EncryptedSharedPreferences`) |
| **Networking** | Retrofit 3.0, OkHttp 4.12 |
| **Serialization** | `kotlinx.serialization` (JSON) |
| **Image Loading** | Coil 3 (`coil3`) |
| **Static Analysis** | Detekt & KtLint |
| **Testing** | JUnit 4, Robolectric, MockWebServer, Kotlinx Coroutines Test |

---

## Building & Running

### Prerequisites
- Android Studio Ladybug (or higher)
- JDK 17
- Android SDK 37 (Compile SDK 37, Target SDK 37, Min SDK 26)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/your-username/BacklogOrder.git
cd BacklogOrder

# Run unit tests
./gradlew testDebugUnitTest

# Run code style and static checks
./gradlew ktlintCheck detekt

# Assemble debug APK
./gradlew assembleDebug
```

### RAWG API Setup
1. Obtain a free API key at [RAWG.io](https://rawg.io/apidocs).
2. On first launch, enter your API key in the **Onboarding Screen**, or navigate to **Settings** at any time.
