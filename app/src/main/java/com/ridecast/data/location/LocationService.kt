package com.ridecast.data.location

import com.google.android.gms.maps.model.LatLng
import com.ridecast.core.util.Result

interface LocationService {
    suspend fun getCurrentLocation(): Result<LatLng>
}
