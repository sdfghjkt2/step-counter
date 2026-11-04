package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailyGoal: Int = 10000,
    val strideLengthMeters: Double = 0.762, // Default ~30 inches / 76 cm
    val weightKg: Double = 70.0,
    val heightCm: Double = 175.0,
    val healthConnectAutoSync: Boolean = true,
    val healthConnectSyncIntervalMinutes: Int = 30,
    val useMetricUnits: Boolean = true
)
