# AGENTS.md — RideCast

Canonical entry point for AI agents and human contributors working on **RideCast**, an Android app that shows motorcyclists weather along their planned route — not just at the start.

## What this repo is

- **App:** RideCast (`com.ridecast`, debug suffix `.debug`)
- **Platform:** Android only — single `:app` module, Kotlin, Jetpack Compose, Material 3
- **Status:** Milestones 1–10 complete (route planning → weather timeline → map markers → summary/insights → persistence → polish/tests)

## Documentation map

| Document | Contents |
|----------|----------|
| [README.md](README.md) | User-facing overview, quick setup, feature list |
| [docs/architecture.md](docs/architecture.md) | Layers, navigation, ViewModel scoping, data flow |
| [docs/development.md](docs/development.md) | Build/run, API keys, emulator setup, troubleshooting |
| [docs/decisions/](docs/decisions/) | Architecture decision records (ADRs) |

**Rule for agents:** Put durable project knowledge in `AGENTS.md` or `docs/`. Do not bury project conventions in tool-specific config.

## Repository layout

```
WeatherMap/                    # repo root (project name in settings.gradle.kts: RideCast)
├── app/src/main/java/com/ridecast/
│   ├── core/                  # Result<T>, NetworkMonitor, PolylineDecoder, Flow extensions
│   ├── domain/                # Models, repository interfaces, use cases, insight engine
│   ├── data/                  # Retrofit, Room, DataStore, repository implementations
│   ├── di/                    # Hilt modules (AppModule, InsightModule)
│   └── presentation/          # Compose screens, ViewModels, navigation, theme
├── gradle/libs.versions.toml  # Version catalog — single source for dependency versions
├── local.properties.example # API key template (copy to local.properties)
└── docs/                      # Deep-dive reference material
```

Package naming follows Clean Architecture: dependencies point inward (`presentation` → `domain` ← `data`).

## Tech stack (verified)

| Area | Choice |
|------|--------|
| Language | Kotlin 2.1.21, JVM 17 |
| UI | Jetpack Compose (BOM 2025.05.01), Material 3 |
| DI | Hilt 2.55 (`kapt`) |
| Async | Coroutines + Flow |
| Network | Retrofit 2.11, OkHttp 4.12 |
| Local storage | Room 2.7.1, DataStore 1.1.4 |
| Maps | Google Maps Compose 6.2.1, Places SDK 4.1.0, Routes API |
| Weather | WeatherAPI.com (`api.weatherapi.com/v1/`) |
| Logging | Timber (debug tree only) |
| Tests | JUnit 4, Compose UI tests, Espresso |

## Build and test

```bash
./gradlew assembleDebug          # debug APK
./gradlew test                     # unit tests (no device)
./gradlew connectedAndroidTest     # instrumented tests (device/emulator)
```

**Requirements:** JDK 17, Android SDK API 26–35, `compileSdk`/`targetSdk` 35. See [docs/development.md](docs/development.md) for `JAVA_HOME`, emulator, and API key setup.

**API keys:** Never commit `local.properties`. Keys are loaded at build time into `BuildConfig.MAPS_API_KEY` and `BuildConfig.WEATHER_API_KEY` via `app/build.gradle.kts`.

## Key conventions

### Architecture

- **Clean Architecture + MVVM:** Screens observe `StateFlow`; ViewModels call use cases/repositories.
- **`Result<T>`** (`core/util/Result.kt`): `Loading`, `Success(data)`, `Error(exception, message)` — used across all layers.
- **Repository pattern:** Interfaces in `domain/repository/`, implementations in `data/repository/`, bound in `di/AppModule.kt`.
- **Use cases:** `SampleRouteUseCase`, `GetRideWeatherUseCase` — orchestrate domain logic.
- **Insight engine:** `RideInsightEngine` interface with `DeterministicInsightEngine` default; bound in `di/InsightModule.kt`.

### Navigation and shared state

- **Single Activity:** `MainActivity` hosts bottom nav + `RideCastNavGraph`.
- **Hoisted ViewModels** (via `hiltViewModel()` at NavHost level in `RideCastNavGraph`):
  - `RouteViewModel`, `WeatherViewModel` — shared across Plan, Timeline, Map, Summary
  - `SettingsViewModel` — sampling interval propagates to `RouteViewModel`
  - `SummaryViewModel` — summary/insights screen
- **Weather fetch** is triggered in `RideCastNavGraph` via `LaunchedEffect(routeState)` so it survives tab navigation (not in `TripPlannerScreen`).
- **Route initial state:** `RouteViewModel` uses `Result.Error(null, null)` as "not yet calculated" sentinel (not `Loading`).

### External APIs

- **Google:** Maps SDK, Places API (New) via `Places.initializeWithNewPlacesApiEnabled`, Routes API at `routes.googleapis.com`
- **Weather:** Forecast endpoint per sampled waypoint at expected ETA
- **Two Retrofit instances:** default → WeatherAPI; `@Named("routes")` → Google Routes

### Code style

- Official Kotlin code style (`gradle.properties`)
- KDoc on public types and non-obvious logic
- Google/Android types stay in `presentation` and `data` — not in `domain`
- Conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`

## Screens

| Tab | Route | Primary responsibility |
|-----|-------|------------------------|
| Plan | `trip_planner` | Places autocomplete, departure time, route calculation |
| Timeline | `timeline` | Animated weather cards along route |
| Map | `map` | Route polyline + color-coded weather markers |
| Summary | `summary` | Stats + deterministic ride insights |
| Settings | `settings` | Sampling distance, units, dark mode, map type (DataStore) |

## What not to change without explicit request

- Application behavior, UI, APIs, dependencies, or business logic
- `local.properties` or committed secrets
- Milestone-scoped architecture unless discussed

## For non-Cursor agents

Read this file first, then `docs/architecture.md` and `docs/development.md`. Cursor-specific tooling behavior lives only in `.cursor/rules/` — it does not duplicate project knowledge.
