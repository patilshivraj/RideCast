package com.ridecast.di

import com.ridecast.data.repository.RouteRepositoryImpl
import com.ridecast.data.repository.WeatherRepositoryImpl
import com.ridecast.domain.repository.RouteRepository
import com.ridecast.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Primary Hilt module that binds domain repository interfaces to their data-layer
 * implementations.
 *
 * Swapping a weather provider (e.g. WeatherAPI → Tomorrow.io) only requires changing
 * the [bindWeatherRepository] binding here — no other code changes needed.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        impl: RouteRepositoryImpl,
    ): RouteRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl,
    ): WeatherRepository
}
