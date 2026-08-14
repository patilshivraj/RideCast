package com.ridecast.data.database

import android.content.Context
import androidx.room.Room
import com.ridecast.data.database.dao.SavedRouteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RideCastDatabase =
        Room.databaseBuilder(context, RideCastDatabase::class.java, "ridecast.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideSavedRouteDao(db: RideCastDatabase): SavedRouteDao = db.savedRouteDao()
}
