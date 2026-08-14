# ADR 002: Places API (New) initialization

**Status:** Accepted  
**Date:** 2026 (post-Milestone 2 fix)

## Context

Google Places SDK 4.x defaults to the **Places API (New)** endpoints. Calling `Places.initialize()` triggers legacy API behavior, which returns error 9011 when only the New API is enabled in Cloud Console.

## Decision

Initialize Places with the New API explicitly in `PlacesModule`:

```kotlin
Places.initializeWithNewPlacesApiEnabled(app, BuildConfig.MAPS_API_KEY)
```

Cloud Console must enable **Places API (New)** (not only the legacy Places API).

## Consequences

- **Positive:** Autocomplete works with current Google Cloud defaults and SDK 4.1.0.
- **Positive:** Aligns with Google's direction for the Places SDK.
- **Negative:** Developers must enable the correct API name in Cloud Console — documented in [development.md](../development.md).
