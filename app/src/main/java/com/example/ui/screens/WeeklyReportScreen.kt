package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DayPerformanceSummary
import com.example.domain.DetailedWeeklyPerformance
import com.example.ui.theme.CyanTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import java.util.Locale

@Composable
fun WeeklyReportScreen(
    performance: DetailedWeeklyPerformance?,
    selectedWeekStart: String,
    onSelectWeekOffset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDaySummary by remember { mutableStateOf<DayPerformanceSummary?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Top Header & Week Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onSelectWeekOffset(-1) },
                        modifier = Modifier.testTag("prev_week_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Week",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "WEEKLY REPORT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = performance?.reportEntity?.let {
                                "${it.weekStartDate} to ${it.weekEndDate}"
                            } ?: "Week of $selectedWeekStart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { onSelectWeekOffset(1) },
                        modifier = Modifier.testTag("next_week_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Week",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (performance == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Generating weekly performance report...",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Health Score & Consistency Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = performance.consistencyRating.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Health Score",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pace: ${performance.walkingPaceCategory}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Score Circle Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(EmeraldPrimary.copy(alpha = 0.3f), EmeraldPrimary.copy(alpha = 0.05f))
                                    )
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${performance.healthScore}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "/ 100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Week-over-Week Change Banner
            item {
                val isPositive = performance.pctChangeVsPrevWeek >= 0
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPositive) EmeraldPrimary.copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositive) EmeraldPrimary else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPositive)
                                        "+${String.format(Locale.US, "%.1f", performance.pctChangeVsPrevWeek)}% vs Last Week"
                                    else
                                        "${String.format(Locale.US, "%.1f", performance.pctChangeVsPrevWeek)}% vs Last Week",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isPositive) EmeraldPrimary else Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Prev Week Total: ${String.format(Locale.US, "%,d", performance.prevWeekTotalSteps)} steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 7-Day Interactive Visual Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "DAILY STEP TRENDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val maxStepInChart = (performance.dailyBreakdown.maxOfOrNull { it.steps } ?: 10000).coerceAtLeast(12000)
                        val targetGoal = performance.dailyBreakdown.firstOrNull()?.goal ?: 10000

                        // Canvas Column Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                performance.dailyBreakdown.forEach { daySummary ->
                                    val isSelected = selectedDaySummary?.dateString == daySummary.dateString
                                    val barHeightRatio = (daySummary.steps.toFloat() / maxStepInChart).coerceIn(0.05f, 1f)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable { selectedDaySummary = daySummary }
                                            .testTag("bar_day_${daySummary.dayOfWeekName}")
                                    ) {
                                        // Step count tooltip if selected
                                        if (isSelected) {
                                            Text(
                                                text = "${daySummary.steps / 1000}k",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        // Bar Canvas
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .fillMaxHeight(barHeightRatio)
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(
                                                    if (daySummary.isGoalMet)
                                                        Brush.verticalGradient(listOf(EmeraldSecondary, EmeraldPrimary))
                                                    else if (isSelected)
                                                        Brush.verticalGradient(listOf(CyanTertiary, CyanTertiary))
                                                    else
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                MaterialTheme.colorScheme.surfaceVariant,
                                                                MaterialTheme.colorScheme.surfaceVariant
                                                            )
                                                        )
                                                )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = daySummary.dayOfWeekName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (daySummary.isGoalMet) FontWeight.Bold else FontWeight.Normal,
                                            color = if (daySummary.isGoalMet) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Goal Achieved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Target: ${String.format(Locale.US, "%,d", targetGoal)} steps/day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4 Core Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportMetricCard(
                            label = "Total Steps",
                            value = String.format(Locale.US, "%,d", performance.reportEntity.totalSteps),
                            subText = "${String.format(Locale.US, "%.1f", performance.reportEntity.totalDistanceKm)} km covered",
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            label = "Daily Average",
                            value = String.format(Locale.US, "%,d", performance.reportEntity.averageDailySteps),
                            subText = "steps / day",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportMetricCard(
                            label = "Peak Step Day",
                            value = String.format(Locale.US, "%,d", performance.reportEntity.peakDaySteps),
                            subText = "Date: ${performance.reportEntity.peakDayDate}",
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            label = "Goal Consistency",
                            value = "${performance.reportEntity.goalMetDays} / 7 Days",
                            subText = "${(performance.reportEntity.goalMetDays * 100) / 7}% success rate",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // AI Smart Performance Analysis Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Performance Report",
                                tint = CyanTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WEEKLY PERFORMANCE ANALYSIS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanTertiary,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = performance.reportEntity.aiSummaryReport,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ACTIONABLE RECOMMENDATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        performance.recommendations.forEach { rec ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = EmeraldSecondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rec,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Day-by-Day Breakdown Header
            item {
                Text(
                    text = "DAY-BY-DAY BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            }

            items(performance.dailyBreakdown) { day ->
                DaySummaryRow(day = day)
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun ReportMetricCard(
    label: String,
    value: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DaySummaryRow(day: DayPerformanceSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (day.isGoalMet) EmeraldPrimary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfWeekName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (day.isGoalMet) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "%,d steps", day.steps),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (day.isGoalMet) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Goal Met",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", day.distanceKm)} km • ${String.format(Locale.US, "%.0f", day.calories)} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", day.pctOfWeeklyTotal)}%",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "of weekly",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
