package com.ridecast.core.util

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

object PolylineDecoder {

    /** Decodes a Google Maps encoded polyline string into a list of LatLng points. */
    fun decode(encodedPolyline: String): List<LatLng> =
        PolyUtil.decode(encodedPolyline)
}
