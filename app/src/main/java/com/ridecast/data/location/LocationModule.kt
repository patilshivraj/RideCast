package com.ridecast.data.location

import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationService(impl: LocationServiceImpl): LocationService

    companion object {

        @Provides
        @Singleton
        fun provideFusedLocationClient(app: Application): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(app)
    }
}
