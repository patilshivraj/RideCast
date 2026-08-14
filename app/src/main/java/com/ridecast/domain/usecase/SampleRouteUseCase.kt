package com.ridecast.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.ridecast.domain.model.RoutePoint
import com.ridecast.domain.model.SamplingConfig
import java.time.ZonedDateTime
import javax.inject.Inject

class SampleRouteUseCase @Inject constructor() {

    operator fun invoke(
        polylinePoints: List<LatLng>,
        totalDistanceMeters: Long,
        totalDurationSeconds: Long,
        departureTime: ZonedDateTime,
        config: SamplingConfig = SamplingConfig(),
    ): List<RoutePoint> {
        if (polylinePoints.isEmpty()) return emptyList()

        val intervalMeters = config.intervalKm * 1000L
        val result = mutableListOf<RoutePoint>()
        var cumulativeDistance = 0.0
        var nextSampleDistance = 0.0   // first sample is always at 0

        for (i in polylinePoints.indices) {
            val point = polylinePoints[i]

            if (i > 0) {
                val segmentDistance = SphericalUtil.computeDistanceBetween(
                    polylinePoints[i - 1], point
                )
                cumulativeDistance += segmentDistance
            }

            if (cumulativeDistance >= nextSampleDistance) {
                result.add(
                    RoutePoint(
                        latitude = point.latitude,
                        longitude = point.longitude,
                        distanceFromStartMeters = cumulativeDistance.toLong(),
                        eta = computeEta(
                            cumulativeDistance, totalDistanceMeters.toDouble(),
                            totalDurationSeconds, departureTime
                        ),
                    )
                )
                nextSampleDistance += intervalMeters
            }
        }

        // Always include the destination if the last sample isn't already very close to it
        val lastPoint = polylinePoints.last()
        val lastSampled = result.lastOrNull()
        if (lastSampled == null ||
            SphericalUtil.computeDistanceBetween(
                LatLng(lastSampled.latitude, lastSampled.longitude), lastPoint
            ) > 1000.0   // more than 1 km gap
        ) {
            result.add(
                RoutePoint(
                    latitude = lastPoint.latitude,
                    longitude = lastPoint.longitude,
                    distanceFromStartMeters = totalDistanceMeters,
                    eta = departureTime.plusSeconds(totalDurationSeconds),
                )
            )
        }

        return result
    }

    private fun computeEta(
        distanceFromStart: Double,
        totalDistance: Double,
        totalDurationSeconds: Long,
        departureTime: ZonedDateTime,
    ): ZonedDateTime {
        if (totalDistance == 0.0) return departureTime
        val ratio = (distanceFromStart / totalDistance).coerceIn(0.0, 1.0)
        val offsetSeconds = (ratio * totalDurationSeconds).toLong()
        return departureTime.plusSeconds(offsetSeconds)
    }
}
