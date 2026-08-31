# RideCast 🏍️⛅

A production-quality Android app that shows motorcyclists the weather they will ride through — not just at the start.

## Features

- 📍 **Route Planning** — Google Places autocomplete for origin and destination
- 🗺️ **Route Calculation** — Google Routes API with traffic-aware ETA
- 🌦️ **Weather Timeline** — Forecast at every 50 km waypoint along the route
- 🗺️ **Weather Map** — Colour-coded markers (green/yellow/blue/red) on the route
- 📊 **Ride Summary** — High/low/avg temp, rain exposure, strong wind sections
- 💡 **Ride Insights** — Deterministic rule-based alerts (LLM-ready interface)
- ⚙️ **Settings** — Sampling distance, units, dark mode, map type — persisted via DataStore

## Setup

### Prerequisites
- Android Studio Ladybug or later
- JDK 17
- Android SDK (API 26–35)

### API Keys

1. Copy `local.properties.example` to `local.properties`
2. Get a **Google Maps Platform** API key from [console.cloud.google.com](https://console.cloud.google.com)
   - Enable: Maps SDK for Android, Places API (New), Routes API
3. Get a **WeatherAPI.com** key from [weatherapi.com](https://www.weatherapi.com) (free tier)
4. Fill in `local.properties`:
```
sdk.dir=/path/to/android/sdk
MAPS_API_KEY=your_google_maps_key
WEATHER_API_KEY=your_weather_key
```

### Build

```bash
./gradlew assembleDebug
```

### Run Tests

```bash
./gradlew test                    # unit tests
./gradlew connectedAndroidTest    # instrumented tests (requires device/emulator)
```

## Architecture

Clean Architecture · MVVM · Repository Pattern · Hilt DI · Kotlin Coroutines + Flow

See [docs/architecture.md](docs/architecture.md) for layers, navigation, data flow, and external APIs.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1.21 |
| UI | Jetpack Compose + Material You |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt 2.55 |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Database | Room 2.7.1 |
| Preferences | DataStore 1.1.4 |
| Maps | Google Maps Compose 6.2.1 |
| Weather | WeatherAPI.com |

## Milestone History

| Milestone | Description |
|---|---|
| 1 | Project scaffold — Hilt, Compose, Navigation, Material You |
| 2 | Google Maps, Places autocomplete, Location permissions |
| 3 | Google Routes API, polyline decode, map rendering |
| 4 | Route sampling (configurable), proportional ETA |
| 5 | WeatherAPI.com integration, parallel fetch, in-memory cache |
| 6 | Animated weather timeline cards |
| 7 | Weather markers on map with tap-to-view bottom sheet |
| 8 | Ride Summary + deterministic AI insight engine |
| 9 | Room database, DataStore settings persistence |
| 10 | UI polish, navigation transitions, shimmer, dark mode wiring, tests |
