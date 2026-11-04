package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun getDailyStepFlow(date: String): Flow<DailyStepEntity?>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getDailyStepSync(date: String): DailyStepEntity?

    @Query("SELECT * FROM daily_steps WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getDailyStepsRangeFlow(startDate: String, endDate: String): Flow<List<DailyStepEntity>>

    @Query("SELECT * FROM daily_steps WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getDailyStepsRangeSync(startDate: String, endDate: String): List<DailyStepEntity>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT 30")
    fun getRecentDailyStepsFlow(): Flow<List<DailyStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStep(dailyStep: DailyStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailySteps(dailySteps: List<DailyStepEntity>)

    // Step logs
    @Query("SELECT * FROM step_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getStepLogsFlow(date: String): Flow<List<StepLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepLog(log: StepLogEntity)

    // Weekly reports
    @Query("SELECT * FROM weekly_reports WHERE weekStartDate = :weekStartDate LIMIT 1")
    fun getWeeklyReportFlow(weekStartDate: String): Flow<WeeklyReportEntity?>

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC")
    fun getAllWeeklyReportsFlow(): Flow<List<WeeklyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyReport(report: WeeklyReportEntity)

    // Settings
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsSync(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserSettings(settings: UserSettingsEntity)
}
