package com.ridecast.data.network

import com.ridecast.data.network.dto.ComputeRoutesRequest
import com.ridecast.data.network.dto.ComputeRoutesResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface RoutesApiService {

    @POST("directions/v2:computeRoutes")
    @Headers("X-Goog-FieldMask: routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline")
    suspend fun computeRoutes(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Body request: ComputeRoutesRequest,
    ): ComputeRoutesResponse
}
