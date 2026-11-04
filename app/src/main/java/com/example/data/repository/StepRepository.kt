package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.DailyStepEntity
import com.example.data.db.StepDao
import com.example.data.db.StepLogEntity
import com.example.data.db.UserSettingsEntity
import com.example.data.db.WeeklyReportEntity
import com.example.data.health.HealthConnectManager
import com.example.data.sensor.StepSensorManager
import com.example.domain.DetailedWeeklyPerformance
import com.example.domain.WeeklyReportGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StepRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    val healthConnectManager: HealthConnectManager = HealthConnectManager(context),
    val stepSensorManager: StepSensorManager = StepSensorManager(context)
) {
    private val stepDao: StepDao = database.stepDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormatter.format(Date())

    val userSettingsFlow: Flow<UserSettingsEntity> = stepDao.getUserSettingsFlow().map {
        it ?: UserSettingsEntity()
    }

    val todayStepFlow: Flow<DailyStepEntity> = stepDao.getDailyStepFlow(getTodayDateString()).map {
        it ?: createInitialDailyStep(getTodayDateString(), 10000, 0.762, 70.0)
    }

    init {
        repositoryScope.launch {
            ensureInitialData()
            healthConnectManager.checkAvailability()
            
            // Listen to sensor events
            stepSensorManager.stepEvents.collect { delta ->
                addStepsToToday(delta, "SENSOR")
            }
        }
    }

    suspend fun ensureInitialData() {
        val todayStr = getTodayDateString()
        var settings = stepDao.getUserSettingsSync()
        if (settings == null) {
            settings = UserSettingsEntity()
            stepDao.insertOrUpdateUserSettings(settings)
        }

        val todayEntity = stepDao.getDailyStepSync(todayStr)
        if (todayEntity == null) {
            // Seed a realistic today step count or create initial record
            val initialStep = createInitialDailyStep(todayStr, settings.dailyGoal, settings.strideLengthMeters, settings.weightKg)
            stepDao.insertOrUpdateDailyStep(initialStep)
        }

        // Seed sample historical data for past 14 days if database is fresh
        val recentSteps = stepDao.getDailyStepsRangeSync("2000-01-01", "2099-12-31")
        if (recentSteps.size <= 1) {
            seedSampleHistory(settings)
        }
    }

    private fun createInitialDailyStep(
        dateStr: String,
        goal: Int,
        strideMeters: Double,
        weightKg: Double,
        steps: Int = 0
    ): DailyStepEntity {
        val distance = steps * strideMeters
        val calories = steps * 0.04 * (weightKg / 70.0)
        val activeMins = steps / 110
        return DailyStepEntity(
            date = dateStr,
            steps = steps,
            goal = goal,
            distanceMeters = distance,
            caloriesBurned = calories,
            activeMinutes = activeMins,
            syncedWithHealthConnect = false,
            lastSyncedTimestamp = 0L
        )
    }

    private suspend fun seedSampleHistory(settings: UserSettingsEntity) {
        val calendar = Calendar.getInstance()
        val sampleList = mutableListOf<DailyStepEntity>()
        val random = java.util.Random(42)

        // Seed last 21 days
        for (i in 21 downTo 1) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormatter.format(calendar.time)
            
            // Generate realistic step count variation around goal (e.g. 5,000 to 14,000)
            val baseSteps = (6000 + random.nextInt(7500))
            val distance = baseSteps * settings.strideLengthMeters
            val calories = baseSteps * 0.04 * (settings.weightKg / 70.0)
            val activeMins = baseSteps / 110

            sampleList.add(
                DailyStepEntity(
                    date = dateStr,
                    steps = baseSteps,
                    goal = settings.dailyGoal,
                    distanceMeters = distance,
                    caloriesBurned = calories,
                    activeMinutes = activeMins,
                    syncedWithHealthConnect = true,
                    lastSyncedTimestamp = System.currentTimeMillis() - (i * 86400000L)
                )
            )
        }
        stepDao.insertOrUpdateDailySteps(sampleList)
    }

    suspend fun addStepsToToday(delta: Int, source: String = "MANUAL") {
        withContext(Dispatchers.IO) {
            val todayStr = getTodayDateString()
            val settings = stepDao.getUserSettingsSync() ?: UserSettingsEntity()
            val current = stepDao.getDailyStepSync(todayStr) ?: createInitialDailyStep(
                todayStr, settings.dailyGoal, settings.strideLengthMeters, settings.weightKg
            )

            val newStepCount = current.steps + delta
            val newDistance = newStepCount * settings.strideLengthMeters
            val newCalories = newStepCount * 0.04 * (settings.weightKg / 70.0)
            val newActiveMins = newStepCount / 110

            val updatedEntity = current.copy(
                steps = newStepCount,
                distanceMeters = newDistance,
                caloriesBurned = newCalories,
                activeMinutes = newActiveMins,
                syncedWithHealthConnect = false
            )

            stepDao.insertOrUpdateDailyStep(updatedEntity)
            
            // Log entry
            stepDao.insertStepLog(
                StepLogEntity(
                    date = todayStr,
                    timestamp = System.currentTimeMillis(),
                    stepDelta = delta,
                    source = source
                )
            )

            // Auto-sync if configured
            if (settings.healthConnectAutoSync && healthConnectManager.isAuthorized.value) {
                syncTodayWithHealthConnect()
            }
        }
    }

    suspend fun setTodaySteps(steps: Int) {
        withContext(Dispatchers.IO) {
            val todayStr = getTodayDateString()
            val settings = stepDao.getUserSettingsSync() ?: UserSettingsEntity()
            val current = stepDao.getDailyStepSync(todayStr) ?: createInitialDailyStep(
                todayStr, settings.dailyGoal, settings.strideLengthMeters, settings.weightKg
            )

            val newDistance = steps * settings.strideLengthMeters
            val newCalories = steps * 0.04 * (settings.weightKg / 70.0)
            val newActiveMins = steps / 110

            val delta = steps - current.steps

            val updatedEntity = current.copy(
                steps = steps,
                distanceMeters = newDistance,
                caloriesBurned = newCalories,
                activeMinutes = newActiveMins,
                syncedWithHealthConnect = false
            )

            stepDao.insertOrUpdateDailyStep(updatedEntity)
            
            if (delta != 0) {
                stepDao.insertStepLog(
                    StepLogEntity(
                        date = todayStr,
                        timestamp = System.currentTimeMillis(),
                        stepDelta = delta,
                        source = "MANUAL_ADJUST"
                    )
                )
            }
        }
    }

    suspend fun syncTodayWithHealthConnect(): Boolean {
        return withContext(Dispatchers.IO) {
            val todayStr = getTodayDateString()
            val todayStep = stepDao.getDailyStepSync(todayStr) ?: return@withContext false
            val success = healthConnectManager.syncSteps(todayStep.steps, todayStr)
            if (success) {
                stepDao.insertOrUpdateDailyStep(
                    todayStep.copy(
                        syncedWithHealthConnect = true,
                        lastSyncedTimestamp = System.currentTimeMillis()
                    )
                )
            }
            success
        }
    }

    fun getWeeklyPerformance(weekStartDate: String): Flow<DetailedWeeklyPerformance> = flow {
        val calendar = Calendar.getInstance()
        val startParsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(weekStartDate) ?: Date()
        calendar.time = startParsed

        // Calculate 7 days for current week
        val currentWeekDays = mutableListOf<String>()
        for (i in 0 until 7) {
            currentWeekDays.add(dateFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val currentWeekStart = currentWeekDays.first()
        val currentWeekEnd = currentWeekDays.last()

        // Calculate 7 days for previous week
        calendar.time = startParsed
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val prevWeekDays = mutableListOf<String>()
        for (i in 0 until 7) {
            prevWeekDays.add(dateFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val prevWeekStart = prevWeekDays.first()
        val prevWeekEnd = prevWeekDays.last()

        val currentWeekSteps = stepDao.getDailyStepsRangeSync(currentWeekStart, currentWeekEnd)
        val prevWeekSteps = stepDao.getDailyStepsRangeSync(prevWeekStart, prevWeekEnd)

        val performance = WeeklyReportGenerator.generateWeeklyReport(
            weekStartDate = currentWeekStart,
            currentWeekSteps = currentWeekSteps,
            previousWeekSteps = prevWeekSteps
        )

        // Save report entity to DB
        stepDao.insertWeeklyReport(performance.reportEntity)

        emit(performance)
    }

    fun getTodayLogsFlow(): Flow<List<StepLogEntity>> {
        return stepDao.getStepLogsFlow(getTodayDateString())
    }

    suspend fun updateSettings(newSettings: UserSettingsEntity) {
        withContext(Dispatchers.IO) {
            stepDao.insertOrUpdateUserSettings(newSettings)
            // Update today's goal and values
            val todayStr = getTodayDateString()
            val current = stepDao.getDailyStepSync(todayStr)
            if (current != null) {
                val newDistance = current.steps * newSettings.strideLengthMeters
                val newCalories = current.steps * 0.04 * (newSettings.weightKg / 70.0)
                stepDao.insertOrUpdateDailyStep(
                    current.copy(
                        goal = newSettings.dailyGoal,
                        distanceMeters = newDistance,
                        caloriesBurned = newCalories
                    )
                )
            }
        }
    }

    suspend fun regenerateSampleData() {
        withContext(Dispatchers.IO) {
            val settings = stepDao.getUserSettingsSync() ?: UserSettingsEntity()
            seedSampleHistory(settings)
        }
    }
}
