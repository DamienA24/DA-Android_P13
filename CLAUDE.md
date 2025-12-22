# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hexagonal Games is an Android application built with Kotlin and Jetpack Compose. The app follows a clean architecture pattern with a focus on separation of concerns between data, domain, and UI layers.

## Build & Development Commands

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run tests for a specific variant
./gradlew testDebugUnitTest
```

### Building APK
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Running Lint
```bash
./gradlew lint
```

### Clean Build
```bash
./gradlew clean
```

## Project Architecture

### Package Structure
- `com.openclassrooms.hexagonal.games`
  - `data/` - Data layer (repositories, API services)
    - `repository/` - Repository implementations
    - `service/` - API interfaces and implementations
  - `domain/` - Domain layer (business models)
    - `model/` - Data models (Post, User)
  - `screen/` - UI screens organized by feature
    - Each screen has its own package with Screen and ViewModel
  - `ui/` - UI components and theming
    - `theme/` - Theme configuration (Color, Type, Theme)
  - `di/` - Dependency injection modules

### Architecture Pattern
The app follows a **layered architecture** with:

1. **Data Layer** (`data/`)
   - `PostApi` interface defines the contract for data operations
   - `PostFakeApi` provides an in-memory implementation for testing
   - `PostRepository` acts as the single source of truth, exposing data via Kotlin Flow

2. **Domain Layer** (`domain/`)
   - Contains pure domain models (`Post`, `User`)
   - Models are immutable data classes

3. **UI Layer** (`screen/` and `ui/`)
   - Each feature has its own screen package with:
     - `*Screen.kt` - Composable UI
     - `*ViewModel.kt` - State management and business logic
   - Follows MVVM pattern with ViewModel handling state
   - Uses Jetpack Compose for declarative UI

### Dependency Injection
- Uses **Hilt** for dependency injection
- `AppModule` provides singleton dependencies
- All ViewModels and repositories are injected via Hilt
- Current configuration: `PostApi` is bound to `PostFakeApi` for testing

### Navigation
- Uses Jetpack Navigation Compose
- Navigation graph defined in `MainActivity.kt` via `HexagonalGamesNavHost`
- Screen routes defined in sealed class `Screen.kt`
- Current screens: Homefeed (start destination), AddPost, Settings

### Key Screens
- **Homefeed**: Main feed displaying posts
- **AddPost**: Form to create new posts
- **Settings**: App settings screen

## Technology Stack

- **Language**: Kotlin 1.9.23
- **Build System**: Gradle with Kotlin DSL
- **UI Framework**: Jetpack Compose (BOM 2024.04.00)
- **DI**: Hilt 2.49
- **Image Loading**: Coil Compose
- **Async**: Kotlin Coroutines
- **Navigation**: Navigation Compose
- **Permissions**: Accompanist Permissions
- **Firebase**: Analytics (via Google Services)
- **Min SDK**: 24
- **Target/Compile SDK**: 34

## Important Notes

### Data Source
The app currently uses `PostFakeApi` as the data source, which provides in-memory fake data. This is configured in `AppModule.kt`. To switch to a real API implementation, update the `providePostApi()` method.

### Version Catalog
All dependencies are managed via Gradle Version Catalog in `gradle/libs.versions.toml`. When adding new dependencies, add them to the version catalog first.

### Compose Compiler
The project uses Compose Compiler Extension version 1.5.11, which must be compatible with the Kotlin version (1.9.23).

Always use context7 when I need code generation, setup or configuration steps, or
library/API documentation. This means you should automatically use the Context7 MCP
tools to resolve library id and get library docs without me having to explicitly ask.
