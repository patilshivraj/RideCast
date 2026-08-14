package com.ridecast.di

import com.ridecast.domain.insight.DeterministicInsightEngine
import com.ridecast.domain.insight.RideInsightEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InsightModule {

    @Binds
    @Singleton
    abstract fun bindRideInsightEngine(impl: DeterministicInsightEngine): RideInsightEngine
}
