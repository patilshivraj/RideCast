# ADR 003: Pluggable insight engine interface

**Status:** Accepted  
**Date:** 2026 (Milestone 8)

## Context

Ride Summary needs human-readable insights about weather along the route. An LLM-backed engine may be added later, but the app must ship with deterministic, testable behavior now.

## Decision

Define `RideInsightEngine` in `domain/insight/`:

```kotlin
interface RideInsightEngine {
    suspend fun generateInsights(
        rideWeather: RideWeather,
        originName: String,
        destinationName: String,
    ): List<String>
}
```

Default implementation: `DeterministicInsightEngine` (rule-based thresholds via `WeatherConditionClassifier`). Bound in `di/InsightModule.kt`.

`SummaryViewModel` depends only on the interface.

## Consequences

- **Positive:** UI and domain unchanged when swapping to OpenAI, Gemini, or on-device models.
- **Positive:** `DeterministicInsightEngineTest` provides stable unit coverage.
- **Negative:** Interface is `suspend` to accommodate future async LLM calls even though the current impl is synchronous.
