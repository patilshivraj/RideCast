package com.ridecast.presentation.map

import com.google.maps.android.compose.MapType
import com.ridecast.domain.model.MapDisplayType

/** Maps persisted [MapDisplayType] to Google Maps Compose [MapType]. */
fun MapDisplayType.toComposeMapType(): MapType = when (this) {
    MapDisplayType.NORMAL -> MapType.NORMAL
    MapDisplayType.SATELLITE -> MapType.SATELLITE
    MapDisplayType.TERRAIN -> MapType.TERRAIN
    MapDisplayType.HYBRID -> MapType.HYBRID
}
