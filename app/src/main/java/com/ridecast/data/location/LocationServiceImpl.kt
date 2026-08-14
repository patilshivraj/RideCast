package com.ridecast.data.location

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.ridecast.core.util.Result
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class LocationServiceImpl @Inject constructor(
    private val fusedClient: FusedLocationProviderClient,
) : LocationService {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LatLng> = try {
        val location = fusedClient.lastLocation.await()
        if (location != null) {
            Result.Success(LatLng(location.latitude, location.longitude))
        } else {
            Result.Error(Exception("Location unavailable — ensure GPS is enabled"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to retrieve current location")
        Result.Error(e)
    }
}
