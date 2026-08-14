# Architecture

RideCast follows **Clean Architecture** with **MVVM** in the presentation layer. All Kotlin sources live under `app/src/main/java/com/ridecast/`.

## Layer diagram

```
┌─────────────────────────────────────────────────────────┐
│  presentation/                                          │
│  Compose screens · ViewModels · Navigation · Theme      │
└──────────────────────────┬──────────────────────────────┘
                           │ observes StateFlow, calls
┌──────────────────────────▼──────────────────────────────┐
│  domain/                                                │
│  Models · Repository interfaces · Use cases · Insights  │
└──────────────────────────┬──────────────────────────────┘
                           │ implemented by
┌──────────────────────────▼──────────────────────────────┐
│  data/                                                  │
│  Retrofit · Room · DataStore · Repository impls         │
└─────────────────────────────────────────────────────────┘

        core/  — shared utilities (Result<T>, extensions)
        di/    — Hilt bindings
```

**Dependency rule:** `presentation` and `data` depend on `domain`. `domain` has no Android or framework imports (except `SampleRouteUseCase`, which uses `LatLng`/`SphericalUtil` for geodesic sampling).

## Package reference

| Package | Key types |
|---------|-----------|
| `core.util` | `Result<T>`, `PolylineDecoder`, `NetworkMonitor` |
| `core.extension` | `FlowExtensions` |
| `domain.model` | `Route`, `RoutePoint`, `TripInput`, `RideWeather`, `WeatherData`, `AppSettings`, `SamplingConfig` |
| `domain.repository` | `RouteRepository`, `WeatherRepository`, `SettingsRepository` |
| `domain.usecase` | `SampleRouteUseCase`, `GetRideWeatherUseCase` |
| `domain.insight` | `RideInsightEngine`, `DeterministicInsightEngine`, `WeatherConditionClassifier` |
| `data.network` | `WeatherApiService`, `RoutesApiService`, DTOs, `ApiModule`, `RoutesApiModule` |
| `data.repository` | `RouteRepositoryImpl`, `WeatherRepositoryImpl` |
| `data.database` | `RideCastDatabase`, `SavedRouteEntity`, `SavedRouteDao` |
| `data.settings` | `DataStoreSettingsRepository` |
| `data.places` | `PlacesModule` |
| `data.location` | `LocationService`, `LocationServiceImpl` |
| `presentation.navigation` | `Screen`, `RideCastNavGraph` |
| `presentation.*` | Feature screens and ViewModels |

## Navigation and ViewModel scoping

`MainActivity` creates a `NavHostController` and bottom navigation over five tabs defined in `Screen.bottomNavTabs`.

`RideCastNavGraph` instantiates shared ViewModels **once** at the NavHost level:

```kotlin
val routeViewModel: RouteViewModel = hiltViewModel()
val weatherViewModel: WeatherViewModel = hiltViewModel()
val summaryViewModel: SummaryViewModel = hiltViewModel()
val settingsViewModel: SettingsViewModel = hiltViewModel()
```

This lets Plan, Timeline, Map, and Summary read the same route and weather state without a shared Activity-scoped holder.

### Side effects owned by the nav graph

1. **Sampling config sync** — `LaunchedEffect(settingsState.samplingIntervalKm)` calls `routeViewModel.updateSamplingConfig()` to re-sample waypoints when the user changes interval in Settings.

2. **Weather fetch** — `LaunchedEffect(routeState)` watches `RouteViewModel.routeState` and calls `weatherViewModel.fetchWeather()` on `Result.Success`. This must live in the nav graph (not a single screen) because the user may navigate away from Plan before the async Routes API returns.

## End-to-end data flow

```
TripPlannerScreen
    │ user selects origin/destination (Places SDK)
    │ user taps Calculate Ride
    ▼
RouteViewModel.calculateRoute(TripInput)
    ▼
RouteRepositoryImpl → Google Routes API (computeRoutes)
    │ returns encoded polyline, distance, duration
    ▼
PolylineDecoder.decode() → List<LatLng>
    ▼
SampleRouteUseCase → List<RoutePoint>  (waypoints every N km with proportional ETA)
    ▼
RouteViewModel emits Result.Success(RouteUiModel)
    ▼
RideCastNavGraph LaunchedEffect → WeatherViewModel.fetchWeather()
    ▼
GetRideWeatherUseCase → parallel WeatherRepository.getWeatherForPoint() per waypoint
    │ WeatherRepositoryImpl caches by lat/lon/hour key
    ▼
Result.Success(RideWeather) → Timeline / Map / Summary screens
    ▼
SummaryViewModel.computeSummary() + RideInsightEngine.generateInsights()
```

## Result<T> pattern

`core/util/Result.kt` is a sealed class used consistently:

- **Loading** — operation in progress
- **Success(data)** — completed with payload
- **Error(exception, message)** — failure

Helpers: `isLoading`, `isSuccess`, `isError`, `getOrNull()`, `map()`.

**Sentinel usage:** `RouteViewModel` initializes with `Result.Error(null, null)` meaning "no route calculated yet". UI treats `Error` with `message == null` as idle, not a user-visible failure.

## Dependency injection (Hilt)

| Module | Scope | Provides |
|--------|-------|----------|
| `di/AppModule` | Singleton | `RouteRepository`, `WeatherRepository` bindings |
| `di/InsightModule` | Singleton | `RideInsightEngine` → `DeterministicInsightEngine` |
| `data/network/ApiModule` | Singleton | OkHttp, Weather Retrofit, `WeatherApiService` |
| `data/network/RoutesApiModule` | Singleton | `@Named("routes")` Retrofit, `RoutesApiService` |
| `data/places/PlacesModule` | Singleton | `PlacesClient` (New Places API) |
| `data/database/DatabaseModule` | Singleton | Room database |
| `data/settings/SettingsModule` | Singleton | Settings repository |
| `data/location/LocationModule` | Singleton | `LocationService` |

Entry point: `@HiltAndroidApp` on `RideCastApplication`.

## External services

### Google Maps Platform

- **Maps SDK for Android** — map rendering in `MapScreen`
- **Places API (New)** — autocomplete in `TripPlannerViewModel`; initialized with `Places.initializeWithNewPlacesApiEnabled`
- **Routes API** — `POST https://routes.googleapis.com/directions/v2:computeRoutes` via `RoutesApiService`

API key: `BuildConfig.MAPS_API_KEY` (from `local.properties` → `app/build.gradle.kts`).

### WeatherAPI.com

- Base URL: `https://api.weatherapi.com/v1/`
- Forecast per waypoint: `WeatherApiService.getForecast()` with lat/lon, date, hour
- In-memory `ConcurrentHashMap` cache in `WeatherRepositoryImpl`

API key: `BuildConfig.WEATHER_API_KEY`.

## Persistence

| Store | Purpose |
|-------|---------|
| **DataStore** | User settings (sampling interval, units, dark mode, map type) |
| **Room** | Saved routes (`SavedRouteEntity`, `SavedRouteDao`) — Milestone 9 |

## Testing

| Location | Coverage |
|----------|----------|
| `app/src/test/` | `SampleRouteUseCaseTest`, `DeterministicInsightEngineTest` |
| `app/src/androidTest/` | `TripPlannerScreenTest` (Compose UI) |

Domain use cases and insight engine are the primary unit-test targets. Network and Maps are not mocked in the current test suite.

## Related decisions

- [Single app module](decisions/001-single-app-module.md)
- [Places API (New) initialization](decisions/002-places-api-new.md)
- [Pluggable insight engine](decisions/003-insight-engine-interface.md)
