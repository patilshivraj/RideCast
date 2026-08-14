package com.ridecast.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_routes")
data class SavedRouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destinationName: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val encodedPolyline: String,
    val savedAt: Long,
)
