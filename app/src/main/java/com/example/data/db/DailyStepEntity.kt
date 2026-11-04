package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val steps: Int,
    val goal: Int,
    val distanceMeters: Double,
    val caloriesBurned: Double,
    val activeMinutes: Int,
    val syncedWithHealthConnect: Boolean = false,
    val lastSyncedTimestamp: Long = 0L
)
