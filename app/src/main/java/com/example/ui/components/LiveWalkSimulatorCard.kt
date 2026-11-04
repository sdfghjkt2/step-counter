package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.LiveWalkSimulationState
import com.example.ui.theme.CyanTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import java.util.Locale

@Composable
fun LiveWalkSimulatorCard(
    liveState: LiveWalkSimulationState,
    onStartSimulation: (targetSteps: Int, durationSeconds: Int) -> Unit,
    onPauseSimulation: () -> Unit,
    onResumeSimulation: () -> Unit,
    onStopSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTargetSteps by remember { mutableIntStateOf(1000) }
    var selectedDurationSec by remember { mutableIntStateOf(30) }
    var customStepsText by remember { mutableStateOf("") }
    var customDurationText by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Live Walk Simulation",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE WALK SIMULATOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = CyanTertiary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Auto-Sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanTertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Simulate Real-Time Walking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!liveState.isRunning) {
                // Configuration View
                Text(
                    text = "TARGET STEPS TO ADD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Step Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500, 1000, 2500, 5000).forEach { steps ->
                        val isSelected = !isCustomMode && selectedTargetSteps == steps
                        Surface(
                            onClick = {
                                selectedTargetSteps = steps
                                isCustomMode = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_target_steps_$steps")
                        ) {
                            Text(
                                text = if (steps >= 1000) "${steps / 1000}k" else "$steps",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "TIME DURATION TO COMPLETE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Duration Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15 to "15s", 30 to "30s", 60 to "1m", 120 to "2m", 300 to "5m").forEach { (sec, label) ->
                        val isSelected = !isCustomMode && selectedDurationSec == sec
                        Surface(
                            onClick = {
                                selectedDurationSec = sec
                                isCustomMode = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyanTertiary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_duration_$sec")
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Inputs Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCustomMode) "Custom Target & Time Mode" else "Preset Mode (${selectedTargetSteps} steps in ${selectedDurationSec}s)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isCustomMode) "Use Presets" else "Custom Inputs",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanTertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isCustomMode = !isCustomMode }
                            .padding(4.dp)
                    )
                }

                if (isCustomMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customStepsText,
                            onValueChange = { customStepsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Steps") },
                            placeholder = { Text("e.g. 1500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_steps_input")
                        )
                        OutlinedTextField(
                            value = customDurationText,
                            onValueChange = { customDurationText = it.filter { c -> c.isDigit() } },
                            label = { Text("Seconds") },
                            placeholder = { Text("e.g. 45") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_duration_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Start Simulation Button
                Button(
                    onClick = {
                        val stepsToRun = if (isCustomMode) (customStepsText.toIntOrNull() ?: 1000) else selectedTargetSteps
                        val durToRun = if (isCustomMode) (customDurationText.toIntOrNull() ?: 30) else selectedDurationSec
                        onStartSimulation(stepsToRun.coerceAtLeast(10), durToRun.coerceAtLeast(5))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_walk_simulation_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Simulation",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Walk Simulation & Live Sync",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            } else {
                // ACTIVE LIVE SIMULATION RUNNING VIEW
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Status Header
                    Surface(
                        shape = CircleShape,
                        color = if (liveState.isPaused) Color(0xFFF59E0B).copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (liveState.isPaused) Color(0xFFF59E0B) else EmeraldPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = if (liveState.isPaused) Color(0xFFF59E0B) else EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (liveState.isPaused) "PAUSED" else "LIVE WALK ACTIVE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (liveState.isPaused) Color(0xFFF59E0B) else EmeraldPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Live Counter Readout
                    Text(
                        text = "+${String.format(Locale.US, "%,d", liveState.addedSteps)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = EmeraldPrimary
                    )

                    Text(
                        text = "of ${String.format(Locale.US, "%,d", liveState.targetSteps)} target steps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { liveState.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Details Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = CyanTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${liveState.remainingSeconds}s remaining",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Cadence: ~${liveState.currentCadenceSpm} spm",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = liveState.statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulation Action Controls (Pause/Resume, Stop)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (liveState.isPaused) {
                            Button(
                                onClick = onResumeSimulation,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("resume_simulation_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = onPauseSimulation,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pause_simulation_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onStopSimulation,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stop_simulation_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
