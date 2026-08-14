# Development guide

## Prerequisites

| Requirement | Version / notes |
|-------------|-----------------|
| Android Studio | Ladybug or later recommended |
| JDK | 17 (`compileOptions` / `kotlinOptions.jvmTarget` in `app/build.gradle.kts`) |
| Android SDK | API 26 (min) through 35 (compile/target) |
| Device/emulator | Google Play Services required (Maps, Places, Fused Location) |

### JAVA_HOME

Gradle and Android Studio both need JDK 17 on the path.

```bash
# macOS — verify
java -version    # should report 17.x
echo $JAVA_HOME

# If unset, point to Android Studio's bundled JBR or a system JDK 17:
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

Android Studio usually configures `sdk.dir` in `local.properties` automatically.

## First-time setup

1. Clone the repository.
2. Copy the API key template:
   ```bash
   cp local.properties.example local.properties
   ```
3. Fill in `local.properties` (see [API keys](#api-keys)). **Never commit this file.**
4. Open the project in Android Studio and sync Gradle.
5. Create an AVD with a **Google Play** system image (API 26+).

## API keys

Keys are read from `local.properties` at build time and injected into `BuildConfig` and the manifest placeholder for Maps.

```properties
sdk.dir=/path/to/android/sdk
MAPS_API_KEY=your_google_maps_api_key
WEATHER_API_KEY=your_weather_api_key
```

### Google Maps Platform

1. Create a project at [Google Cloud Console](https://console.cloud.google.com).
2. Enable these APIs:
   - **Maps SDK for Android**
   - **Places API (New)** — not the legacy "Places API"
   - **Routes API**
3. Create an API key. Restrict it to:
   - Android apps → package `com.ridecast` (and `com.ridecast.debug` for debug builds)
   - SHA-1 fingerprint of your debug/release keystore

### WeatherAPI.com

1. Sign up at [weatherapi.com](https://www.weatherapi.com) (free tier: ~1M calls/month).
2. Copy the API key from the dashboard into `WEATHER_API_KEY`.

After changing keys, rebuild — keys are compile-time constants in `BuildConfig`.

## Build commands

```bash
# From repo root
./gradlew assembleDebug

# Output APK
# app/build/outputs/apk/debug/app-debug.apk

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Unit tests (JVM, no device)
./gradlew test

# Instrumented tests (requires running device/emulator)
./gradlew connectedAndroidTest

# Clean rebuild
./gradlew clean assembleDebug
```

Debug builds use `applicationIdSuffix = ".debug"` → package `com.ridecast.debug`.

## Gradle notes

- **Version catalog:** `gradle/libs.versions.toml` — add or bump dependencies here.
- **Configuration cache:** enabled in `gradle.properties` (`org.gradle.configuration-cache=true`).
- **Kapt:** Hilt and Room use `kapt` (`kapt.correctErrorTypes=true`).

## Running in Android Studio

1. Select a Google Play emulator or physical device.
2. Run the `app` configuration.
3. Grant location permission when prompted (Plan screen / current location).

## Common pitfalls

### Places autocomplete fails (error 9011 / "legacy API")

**Symptom:** Autocomplete returns no results or logs mention legacy Places API.

**Causes & fixes:**
1. Enable **Places API (New)** in Google Cloud Console (not only the legacy Places API).
2. Code must call `Places.initializeWithNewPlacesApiEnabled()` — see `data/places/PlacesModule.kt`. Plain `Places.initialize()` targets the legacy API.
3. Wait a few minutes after enabling a new API in Cloud Console before retesting.
4. Verify API key restrictions include your app's package name and SHA-1.

### Weather timeline/map/summary empty after route calculation

**Symptom:** Route calculates successfully but weather tabs show loading or empty state.

**Cause:** Weather fetch was previously triggered from `TripPlannerScreen` via `LaunchedEffect(routeState)`. Navigating away from Plan before the Routes API completes disposes that effect.

**Fix (already in codebase):** Weather fetch lives in `RideCastNavGraph` `LaunchedEffect(routeState)`, which stays alive across tab switches.

### "Calculate Ride" button stuck on "Calculating…"

**Symptom:** Plan screen shows calculating state on first launch before any action.

**Cause:** `RouteViewModel` previously initialized `routeState` to `Result.Loading`.

**Fix (already in codebase):** Initial state is `Result.Error(null, null)` (idle sentinel). UI only shows errors when `message != null`.

### Routes API returns no route

- Confirm **Routes API** is enabled for the same Cloud project as the API key.
- Check Logcat for OkHttp logs (enabled in debug via `ApiModule`).
- Verify origin/destination coordinates are valid (Places fetch must succeed first).

### Maps render blank

- Confirm **Maps SDK for Android** is enabled.
- Check `AndroidManifest.xml` meta-data uses `${MAPS_API_KEY}` placeholder.
- Emulator must have Google Play Services.

### Build fails: missing SDK or JDK

- Set `sdk.dir` in `local.properties`.
- Ensure `JAVA_HOME` points to JDK 17.

## Debugging tips

- **Logcat tag:** Filter `OkHttp` for network traffic (body logging in debug only).
- **Timber:** Debug tree planted in `RideCastApplication` — route sampling logs in `RouteViewModel`.
- **Rebuild after key changes:** `BuildConfig` keys are compile-time; a hot reload is not enough.

## Commit conventions

Use conventional commits:

```
feat: add offline route cache
fix: handle empty sample points in weather fetch
docs: update API key setup
test: cover rain exposure calculation
chore: bump compose BOM
```

Reference GitLab issues as `#123` in commit messages when applicable.
