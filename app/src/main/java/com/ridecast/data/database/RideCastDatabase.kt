package com.ridecast.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ridecast.data.database.dao.SavedRouteDao
import com.ridecast.data.database.entity.SavedRouteEntity

@Database(entities = [SavedRouteEntity::class], version = 1, exportSchema = false)
abstract class RideCastDatabase : RoomDatabase() {
    abstract fun savedRouteDao(): SavedRouteDao
}
