package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reports")
data class WeeklyReportEntity(
    @PrimaryKey val weekStartDate: String, // YYYY-MM-DD (Monday)
    val weekEndDate: String,   // YYYY-MM-DD (Sunday)
    val totalSteps: Int,
    val averageDailySteps: Int,
    val peakDayDate: String,
    val peakDaySteps: Int,
    val goalMetDays: Int,
    val totalDistanceKm: Double,
    val totalCalories: Double,
    val totalActiveMinutes: Int,
    val performanceVsPreviousWeekPct: Double,
    val aiSummaryReport: String,
    val generatedTimestamp: Long
)
