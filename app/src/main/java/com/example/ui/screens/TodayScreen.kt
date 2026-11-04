package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DailyStepEntity
import com.example.data.db.StepLogEntity
import com.example.domain.LiveWalkSimulationState
import com.example.ui.components.LiveWalkSimulatorCard
import com.example.ui.theme.CyanTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    dailyStep: DailyStepEntity,
    todayLogs: List<StepLogEntity>,
    isHealthConnectSynced: Boolean,
    isSyncing: Boolean,
    liveWalkState: LiveWalkSimulationState,
    onAddSteps: (Int) -> Unit,
    onSetSteps: (Int) -> Unit,
    onSyncHealthConnect: () -> Unit,
    onNavigateToHealthConnect: () -> Unit,
    onStartWalkSimulation: (targetSteps: Int, durationSeconds: Int) -> Unit,
    onPauseWalkSimulation: () -> Unit,
    onResumeWalkSimulation: () -> Unit,
    onStopWalkSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editStepText by remember { mutableStateOf(dailyStep.steps.toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY's ACTIVITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Health Connect Status Badge
                Surface(
                    onClick = onNavigateToHealthConnect,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isHealthConnectSynced) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isHealthConnectSynced) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("health_connect_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isHealthConnectSynced) Icons.Default.CheckCircle else Icons.Default.Sync,
                            contentDescription = "Health Connect Status",
                            tint = if (isHealthConnectSynced) EmeraldPrimary else CyanTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isHealthConnectSynced) "Health Connect" else "Sync Pending",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isHealthConnectSynced) EmeraldPrimary else CyanTertiary
                        )
                    }
                }
            }
        }

        // Circular Progress Step Ring
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val progressFraction = (dailyStep.steps.toFloat() / dailyStep.goal.toFloat()).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction,
                        animationSpec = tween(durationMillis = 1000),
                        label = "StepProgress"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(230.dp)
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        val primaryColor = EmeraldPrimary
                        val secondaryColor = EmeraldSecondary

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 20.dp.toPx()
                            // Track background ring
                            drawArc(
                                color = trackColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            // Animated active progress ring
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(primaryColor, secondaryColor, CyanTertiary, primaryColor)
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Inner readout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = "Walking",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%,d", dailyStep.steps),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "of ${String.format(Locale.US, "%,d", dailyStep.goal)} goal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val pct = ((dailyStep.steps.toFloat() / dailyStep.goal) * 100).toInt()
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$pct% Completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Adjust Steps Manual Button & Health Connect Sync Button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onSyncHealthConnect() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("sync_health_connect_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSyncing) "Syncing..." else "Sync Health Connect", fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                editStepText = dailyStep.steps.toString()
                                showEditDialog = true
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("edit_steps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit steps",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Live Walk Simulator Card
        item {
            LiveWalkSimulatorCard(
                liveState = liveWalkState,
                onStartSimulation = onStartWalkSimulation,
                onPauseSimulation = onPauseWalkSimulation,
                onResumeSimulation = onResumeWalkSimulation,
                onStopSimulation = onStopWalkSimulation
            )
        }

        // Quick Step Increment Buttons (+100, +500, +1000)
        item {
            Column {
                Text(
                    text = "QUICK ADD STEPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 500, 1000, 2500).forEach { amount ->
                        Button(
                            onClick = { onAddSteps(amount) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_${amount}_steps_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (amount >= 1000) "${amount / 1000}k" else "$amount",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3 Key Fitness Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Distance Card
                MetricTile(
                    title = "Distance",
                    value = String.format(Locale.US, "%.2f", dailyStep.distanceMeters / 1000.0),
                    unit = "km",
                    icon = Icons.Default.DirectionsRun,
                    accentColor = CyanTertiary,
                    modifier = Modifier.weight(1f)
                )
                // Calories Card
                MetricTile(
                    title = "Calories",
                    value = String.format(Locale.US, "%.0f", dailyStep.caloriesBurned),
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                // Active Mins Card
                MetricTile(
                    title = "Active Mins",
                    value = "${dailyStep.activeMinutes}",
                    unit = "mins",
                    icon = Icons.Default.Timer,
                    accentColor = EmeraldSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Activity Log Section
        item {
            Text(
                text = "TODAY's LOGS (${todayLogs.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
        }

        if (todayLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No step logs recorded yet today. Walk or tap quick add above!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(todayLogs.take(5)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (log.stepDelta >= 0) "+${log.stepDelta} steps" else "${log.stepDelta} steps",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Source: ${log.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Manual Edit Steps Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Set Today's Steps") },
            text = {
                Column {
                    Text("Enter total step count for today:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editStepText,
                        onValueChange = { editStepText = it.filter { char -> char.isDigit() } },
                        singleLine = true,
                        modifier = Modifier.testTag("edit_steps_input_field")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = editStepText.toIntOrNull()
                        if (parsed != null && parsed >= 0) {
                            onSetSteps(parsed)
                        }
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("confirm_edit_steps_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
