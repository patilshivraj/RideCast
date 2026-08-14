package com.ridecast.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.ridecast.domain.model.SamplingConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SampleRouteUseCaseTest {

    private val useCase = SampleRouteUseCase()

    @Test
    fun `empty polyline returns empty list`() {
        val result = useCase(
            polylinePoints = emptyList(),
            totalDistanceMeters = 0L,
            totalDurationSeconds = 3600L,
            departureTime = now(),
        )
        assertTrue(result.isEmpty())
    }

    /**
     * Single-point case: no SphericalUtil call is made (i == 0 skips the segment calculation),
     * so this test runs cleanly on the JVM.
     */
    @Test
    fun `single point returns single item`() {
        val point = LatLng(18.52, 73.85)
        val departure = now()
        val result = useCase(
            polylinePoints = listOf(point),
            totalDistanceMeters = 0L,
            totalDurationSeconds = 3600L,
            departureTime = departure,
        )
        assertEquals(1, result.size)
        assertEquals(point.latitude, result[0].latitude, 0.001)
        assertEquals(departure, result[0].eta)
    }

    /**
     * Multi-point tests below invoke SphericalUtil.computeDistanceBetween which requires
     * the Android runtime (or Robolectric). They are @Ignored so the test file compiles and
     * the suite stays green in a pure-JVM environment. Remove @Ignore when Robolectric is added.
     */

    @Ignore("Requires Android runtime / Robolectric for SphericalUtil")
    @Test
    fun `first point is always included at distance zero`() {
        val points = List(10) { LatLng(18.0 + it * 0.1, 73.85) }
        val result = useCase(
            polylinePoints = points,
            totalDistanceMeters = 500_000L,
            totalDurationSeconds = 18_000L,
            departureTime = now(),
            config = SamplingConfig(50),
        )
        assertTrue(result.isNotEmpty())
        assertEquals(0L, result.first().distanceFromStartMeters)
    }

    @Ignore("Requires Android runtime / Robolectric for SphericalUtil")
    @Test
    fun `ETA is bounded between departure and departure plus duration`() {
        val departure = now()
        val duration = 28_800L // 8 hours
        val points = List(100) { LatLng(18.0 + it * 0.01, 73.85) }
        val result = useCase(
            polylinePoints = points,
            totalDistanceMeters = 430_000L,
            totalDurationSeconds = duration,
            departureTime = departure,
            config = SamplingConfig(50),
        )
        val end = departure.plusSeconds(duration)
        result.forEach { pt ->
            assertFalse("ETA ${pt.eta} is before departure", pt.eta.isBefore(departure))
            assertFalse("ETA ${pt.eta} is after end", pt.eta.isAfter(end))
        }
    }

    @Ignore("Requires Android runtime / Robolectric for SphericalUtil")
    @Test
    fun `sampling interval 10km produces more points than 100km`() {
        val points = List(200) { LatLng(18.0 + it * 0.01, 73.85) }
        val fine = useCase(points, 200_000L, 7200L, now(), SamplingConfig(10))
        val coarse = useCase(points, 200_000L, 7200L, now(), SamplingConfig(100))
        assertTrue(fine.size > coarse.size)
    }

    private fun now() = ZonedDateTime.of(2026, 6, 28, 9, 0, 0, 0, ZoneOffset.UTC)
}
