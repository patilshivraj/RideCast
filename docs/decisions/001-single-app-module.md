# ADR 001: Single app module

**Status:** Accepted  
**Date:** 2026 (Milestone 1)

## Context

RideCast is a focused Android app with a clear layer split inside one APK. A multi-module Gradle setup (e.g. `:core`, `:domain`, `:data`, `:app`) would add build complexity without immediate benefit at this scale.

## Decision

Use a **single `:app` module** (`settings.gradle.kts` includes only `:app`) with package-based Clean Architecture:

```
com.ridecast.{core,domain,data,di,presentation}
```

## Consequences

- **Positive:** Simple Gradle setup, fast onboarding, one `assembleDebug` target.
- **Positive:** Layer boundaries enforced by package structure and dependency direction, not module boundaries.
- **Negative:** No compile-time enforcement between layers (relies on discipline and code review).
- **Future:** If the codebase grows significantly, extract `domain` or `data` into library modules without changing package names.
