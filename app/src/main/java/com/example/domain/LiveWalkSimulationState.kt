package com.example.domain

data class LiveWalkSimulationState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val targetSteps: Int = 1000,
    val durationSeconds: Int = 30,
    val addedSteps: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val statusText: String = "Ready to start live walk simulation"
) {
    val progressFraction: Float
        get() = if (targetSteps > 0) (addedSteps.toFloat() / targetSteps).coerceIn(0f, 1f) else 0f

    val remainingSeconds: Int
        get() = ((durationSeconds * 1000L - elapsedTimeMs).coerceAtLeast(0L) / 1000L).toInt()

    val currentCadenceSpm: Int
        get() {
            val seconds = elapsedTimeMs / 1000f
            return if (seconds > 0) ((addedSteps / seconds) * 60f).toInt() else 0
        }
}
