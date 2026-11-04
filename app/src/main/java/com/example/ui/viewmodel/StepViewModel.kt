package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DailyStepEntity
import com.example.data.db.StepLogEntity
import com.example.data.db.UserSettingsEntity
import com.example.data.health.HealthConnectAvailability
import com.example.data.health.HealthConnectSyncLog
import com.example.data.repository.StepRepository
import com.example.domain.DetailedWeeklyPerformance
import com.example.domain.LiveWalkSimulationState
import com.example.domain.WeeklyReportGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StepViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StepRepository(application)

    // Live Walk Simulation State
    private val _liveWalkState = MutableStateFlow(LiveWalkSimulationState())
    val liveWalkState: StateFlow<LiveWalkSimulationState> = _liveWalkState.asStateFlow()
    private var simulationJob: Job? = null

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettingsEntity()
        )

    val todayStep: StateFlow<DailyStepEntity> = repository.todayStepFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyStepEntity(
                date = repository.getTodayDateString(),
                steps = 0,
                goal = 10000,
                distanceMeters = 0.0,
                caloriesBurned = 0.0,
                activeMinutes = 0
            )
        )

    val todayLogs: StateFlow<List<StepLogEntity>> = repository.getTodayLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Week selector state
    private val _selectedWeekStartDate = MutableStateFlow(WeeklyReportGenerator.getStartOfWeekDate())
    val selectedWeekStartDate: StateFlow<String> = _selectedWeekStartDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val weeklyPerformance: StateFlow<DetailedWeeklyPerformance?> = _selectedWeekStartDate
        .flatMapLatest { weekStartDate ->
            repository.getWeeklyPerformance(weekStartDate)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Health Connect States
    val healthConnectAvailability: StateFlow<HealthConnectAvailability> = repository.healthConnectManager.availability
    val healthConnectAuthorized: StateFlow<Boolean> = repository.healthConnectManager.isAuthorized
    val healthConnectSyncing: StateFlow<Boolean> = repository.healthConnectManager.isSyncing
    val healthConnectLogs: StateFlow<List<HealthConnectSyncLog>> = repository.healthConnectManager.syncLogs
    val isSensorAvailable: Boolean = repository.stepSensorManager.isSensorAvailable

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun addQuickSteps(delta: Int) {
        viewModelScope.launch {
            repository.addStepsToToday(delta, "MANUAL")
            _userMessage.value = "+$delta steps added"
        }
    }

    fun setTodaySteps(steps: Int) {
        viewModelScope.launch {
            repository.setTodaySteps(steps)
            _userMessage.value = "Steps updated to $steps"
        }
    }

    fun syncHealthConnect() {
        viewModelScope.launch {
            val success = repository.syncTodayWithHealthConnect()
            if (success) {
                _userMessage.value = "Synced with Google Health Connect!"
            } else {
                _userMessage.value = "Health Connect sync completed"
            }
        }
    }

    fun toggleHealthConnectAuth(granted: Boolean) {
        repository.healthConnectManager.toggleAuthorization(granted)
        _userMessage.value = if (granted) "Google Health Connect Connected" else "Sync Disconnected"
    }

    fun selectWeekOffset(weeksOffset: Int) {
        val calendar = java.util.Calendar.getInstance()
        val currentStr = _selectedWeekStartDate.value
        val parsed = try {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(currentStr)
        } catch (e: Exception) { null }

        if (parsed != null) {
            calendar.time = parsed
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, weeksOffset)
            val newWeekStart = WeeklyReportGenerator.getStartOfWeekDate(calendar.time)
            _selectedWeekStartDate.value = newWeekStart
        }
    }

    fun updateDailyGoal(newGoal: Int) {
        viewModelScope.launch {
            val currentSettings = userSettings.value
            repository.updateSettings(currentSettings.copy(dailyGoal = newGoal))
            _userMessage.value = "Daily goal updated to ${String.format(java.util.Locale.US, "%,d", newGoal)}"
        }
    }

    fun updateUserSettings(strideMeters: Double, weightKg: Double, autoSync: Boolean) {
        viewModelScope.launch {
            val currentSettings = userSettings.value
            repository.updateSettings(
                currentSettings.copy(
                    strideLengthMeters = strideMeters,
                    weightKg = weightKg,
                    healthConnectAutoSync = autoSync
                )
            )
            _userMessage.value = "Settings updated successfully"
        }
    }

    fun startWalkSimulation(targetSteps: Int, durationSeconds: Int) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val totalMs = (durationSeconds.coerceAtLeast(1)) * 1000L
            val intervalMs = 250L // 4 updates per second for realistic animation
            val totalIntervals = (totalMs / intervalMs).coerceAtLeast(1L)

            var currentElapsedMs = 0L
            var accumulatedAddedSteps = 0
            var lastSyncedStepCount = 0

            _liveWalkState.value = LiveWalkSimulationState(
                isRunning = true,
                isPaused = false,
                targetSteps = targetSteps,
                durationSeconds = durationSeconds,
                addedSteps = 0,
                elapsedTimeMs = 0,
                statusText = "Simulating walk & syncing to Health Connect..."
            )

            while (currentElapsedMs < totalMs && accumulatedAddedSteps < targetSteps) {
                delay(intervalMs)

                // Handle pause state
                while (_liveWalkState.value.isPaused) {
                    delay(100L)
                }

                currentElapsedMs += intervalMs
                val expectedStepsAtTime = ((currentElapsedMs.toDouble() / totalMs) * targetSteps).toInt().coerceAtMost(targetSteps)
                val deltaThisInterval = (expectedStepsAtTime - accumulatedAddedSteps).coerceAtLeast(0)

                if (deltaThisInterval > 0) {
                    accumulatedAddedSteps += deltaThisInterval
                    repository.addStepsToToday(deltaThisInterval, "WALK_SIMULATION")

                    // Sync to Health Connect periodically or on major milestones
                    if (accumulatedAddedSteps - lastSyncedStepCount >= 25 || currentElapsedMs >= totalMs || accumulatedAddedSteps >= targetSteps) {
                        lastSyncedStepCount = accumulatedAddedSteps
                        repository.syncTodayWithHealthConnect()
                    }
                }

                _liveWalkState.value = _liveWalkState.value.copy(
                    addedSteps = accumulatedAddedSteps,
                    elapsedTimeMs = currentElapsedMs,
                    statusText = "Syncing live steps to Health Connect (${accumulatedAddedSteps}/$targetSteps)..."
                )
            }

            // Final sync & update state
            repository.syncTodayWithHealthConnect()
            _liveWalkState.value = _liveWalkState.value.copy(
                isRunning = false,
                isPaused = false,
                addedSteps = accumulatedAddedSteps,
                elapsedTimeMs = totalMs,
                statusText = "Simulation Complete! $accumulatedAddedSteps steps added & synced."
            )
            _userMessage.value = "Walk completed! $accumulatedAddedSteps steps added and synced to Google Health Connect."
        }
    }

    fun pauseWalkSimulation() {
        if (_liveWalkState.value.isRunning) {
            _liveWalkState.value = _liveWalkState.value.copy(isPaused = true, statusText = "Walk Simulation Paused")
        }
    }

    fun resumeWalkSimulation() {
        if (_liveWalkState.value.isRunning) {
            _liveWalkState.value = _liveWalkState.value.copy(isPaused = false, statusText = "Resuming Walk Simulation...")
        }
    }

    fun stopWalkSimulation() {
        simulationJob?.cancel()
        _liveWalkState.value = LiveWalkSimulationState(
            isRunning = false,
            isPaused = false,
            statusText = "Walk Simulation Cancelled"
        )
        _userMessage.value = "Walk simulation stopped."
    }

    fun regenerateSampleHistory() {
        viewModelScope.launch {
            repository.regenerateSampleData()
            _userMessage.value = "Sample step history regenerated"
        }
    }
}
