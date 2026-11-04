package com.example.domain

import com.example.data.db.DailyStepEntity
import com.example.data.db.WeeklyReportEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayPerformanceSummary(
    val dayOfWeekName: String, // Mon, Tue, etc.
    val dateString: String,    // YYYY-MM-DD
    val steps: Int,
    val goal: Int,
    val isGoalMet: Boolean,
    val distanceKm: Double,
    val calories: Double,
    val activeMinutes: Int,
    val pctOfWeeklyTotal: Float
)

data class DetailedWeeklyPerformance(
    val reportEntity: WeeklyReportEntity,
    val dailyBreakdown: List<DayPerformanceSummary>,
    val prevWeekTotalSteps: Int,
    val pctChangeVsPrevWeek: Double,
    val healthScore: Int, // 0 - 100
    val consistencyRating: String, // e.g., "Outstanding Consistency"
    val walkingPaceCategory: String, // e.g. "Brisk Walker"
    val recommendations: List<String>
)

object WeeklyReportGenerator {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayNameFormatter = SimpleDateFormat("EEE", Locale.getDefault())

    fun generateWeeklyReport(
        weekStartDate: String, // YYYY-MM-DD (Monday)
        currentWeekSteps: List<DailyStepEntity>,
        previousWeekSteps: List<DailyStepEntity>
    ): DetailedWeeklyPerformance {

        val calendar = Calendar.getInstance()
        val startParsed = try { dateFormatter.parse(weekStartDate) ?: Date() } catch (e: Exception) { Date() }
        calendar.time = startParsed

        // Map by date
        val currentStepMap = currentWeekSteps.associateBy { it.date }
        
        // Build 7 days (Mon -> Sun)
        val daySummaries = mutableListOf<DayPerformanceSummary>()
        var totalSteps = 0
        var totalGoalMetDays = 0
        var totalDistanceMeters = 0.0
        var totalCalories = 0.0
        var totalActiveMins = 0
        var peakSteps = 0
        var peakDate = weekStartDate

        val tempCal = calendar.clone() as Calendar

        for (i in 0 until 7) {
            val dateStr = dateFormatter.format(tempCal.time)
            val dayName = dayNameFormatter.format(tempCal.time)
            val stepRecord = currentStepMap[dateStr] ?: DailyStepEntity(
                date = dateStr,
                steps = 0,
                goal = 10000,
                distanceMeters = 0.0,
                caloriesBurned = 0.0,
                activeMinutes = 0
            )

            val steps = stepRecord.steps
            val goal = stepRecord.goal
            val isMet = steps >= goal && steps > 0

            if (isMet) totalGoalMetDays++
            totalSteps += steps
            totalDistanceMeters += stepRecord.distanceMeters
            totalCalories += stepRecord.caloriesBurned
            totalActiveMins += stepRecord.activeMinutes

            if (steps > peakSteps) {
                peakSteps = steps
                peakDate = dateStr
            }

            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val weekEndDate = dateFormatter.format(Date(tempCal.timeInMillis - 86400000))

        val avgDailySteps = if (totalSteps > 0) totalSteps / 7 else 0
        val totalDistanceKm = totalDistanceMeters / 1000.0

        // Previous week total
        val prevWeekTotalSteps = previousWeekSteps.sumOf { it.steps }
        val pctChange = if (prevWeekTotalSteps > 0) {
            ((totalSteps - prevWeekTotalSteps).toDouble() / prevWeekTotalSteps) * 100.0
        } else if (totalSteps > 0) 100.0 else 0.0

        // Re-loop for percentages
        tempCal.time = startParsed
        for (i in 0 until 7) {
            val dateStr = dateFormatter.format(tempCal.time)
            val dayName = dayNameFormatter.format(tempCal.time)
            val stepRecord = currentStepMap[dateStr] ?: DailyStepEntity(
                date = dateStr, steps = 0, goal = 10000, distanceMeters = 0.0, caloriesBurned = 0.0, activeMinutes = 0
            )
            val steps = stepRecord.steps
            val pctOfWeekly = if (totalSteps > 0) (steps.toFloat() / totalSteps.toFloat()) * 100f else 0f

            daySummaries.add(
                DayPerformanceSummary(
                    dayOfWeekName = dayName,
                    dateString = dateStr,
                    steps = steps,
                    goal = stepRecord.goal,
                    isGoalMet = steps >= stepRecord.goal && steps > 0,
                    distanceKm = stepRecord.distanceMeters / 1000.0,
                    calories = stepRecord.caloriesBurned,
                    activeMinutes = stepRecord.activeMinutes,
                    pctOfWeeklyTotal = pctOfWeekly
                )
            )
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Health Score (0 - 100)
        // Based on average step goal completion (up to 50 pts) + goal met days (up to 30 pts) + active mins (up to 20 pts)
        val goalCompletionScore = (avgDailySteps.toDouble() / 10000.0 * 50.0).coerceAtMost(50.0)
        val goalMetDaysScore = (totalGoalMetDays.toDouble() / 7.0 * 30.0)
        val activeMinsScore = (totalActiveMins.toDouble() / 210.0 * 20.0).coerceAtMost(20.0) // 210 mins = 30m/day WHO recommendation
        val healthScore = (goalCompletionScore + goalMetDaysScore + activeMinsScore).toInt().coerceIn(0, 100)

        val consistencyRating = when {
            totalGoalMetDays >= 6 -> "Elite Consistency"
            totalGoalMetDays >= 4 -> "Strong Active Habit"
            totalGoalMetDays >= 2 -> "Moderate Activity"
            else -> "Needs Momentum"
        }

        val paceCategory = when {
            avgDailySteps >= 12000 -> "Power Walker / Athlete"
            avgDailySteps >= 8500 -> "Active Walker"
            avgDailySteps >= 5000 -> "Moderate Pace"
            else -> "Light Activity"
        }

        val peakDayName = try {
            val pDate = dateFormatter.parse(peakDate)
            if (pDate != null) dayNameFormatter.format(pDate) else "Peak Day"
        } catch (e: Exception) { "Peak Day" }

        // AI Summary Generator
        val aiReportText = buildString {
            append("Weekly Activity Analysis:\n")
            append("• You accumulated a total of ${String.format(Locale.US, "%,d", totalSteps)} steps (${String.format(Locale.US, "%.1f", totalDistanceKm)} km) this week.\n")
            if (pctChange >= 0) {
                append("• Performance is UP by ${String.format(Locale.US, "%.1f", pctChange)}% compared to the previous week.\n")
            } else {
                append("• Performance shifted by ${String.format(Locale.US, "%.1f", pctChange)}% compared to last week.\n")
            }
            append("• Your highest volume occurred on $peakDayName with ${String.format(Locale.US, "%,d", peakSteps)} steps.\n")
            append("• Goal met on $totalGoalMetDays of 7 days (${(totalGoalMetDays * 100) / 7}% consistency rate).\n")
            append("• Total calories burned: ${String.format(Locale.US, "%,.0f", totalCalories)} kcal across ${totalActiveMins} active minutes.")
        }

        val recommendations = mutableListOf<String>()
        if (totalGoalMetDays < 4) {
            recommendations.add("Try setting a 15-minute morning walking routine to jumpstart daily step momentum.")
        } else {
            recommendations.add("Great consistency! Consider pushing your daily target by 500 steps next week.")
        }
        if (totalActiveMins < 150) {
            recommendations.add("Incorporate brisk 10-minute walks post-lunch to reach the recommended 150+ active minutes per week.")
        } else {
            recommendations.add("Your active stamina is excellent! Keep maintaining your cardiovascular rhythm.")
        }
        recommendations.add("Ensure Google Health Connect sync is active to preserve long-term health records.")

        val reportEntity = WeeklyReportEntity(
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate,
            totalSteps = totalSteps,
            averageDailySteps = avgDailySteps,
            peakDayDate = peakDate,
            peakDaySteps = peakSteps,
            goalMetDays = totalGoalMetDays,
            totalDistanceKm = totalDistanceKm,
            totalCalories = totalCalories,
            totalActiveMinutes = totalActiveMins,
            performanceVsPreviousWeekPct = pctChange,
            aiSummaryReport = aiReportText,
            generatedTimestamp = System.currentTimeMillis()
        )

        return DetailedWeeklyPerformance(
            reportEntity = reportEntity,
            dailyBreakdown = daySummaries,
            prevWeekTotalSteps = prevWeekTotalSteps,
            pctChangeVsPrevWeek = pctChange,
            healthScore = healthScore,
            consistencyRating = consistencyRating,
            walkingPaceCategory = paceCategory,
            recommendations = recommendations
        )
    }

    fun getStartOfWeekDate(date: Date = Date()): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        // Set to Monday of this week
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFormatter.format(calendar.time)
    }
}
