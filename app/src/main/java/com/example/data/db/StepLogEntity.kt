package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_logs")
data class StepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long,
    val stepDelta: Int,
    val source: String // "SENSOR", "MANUAL", "HEALTH_CONNECT"
)
