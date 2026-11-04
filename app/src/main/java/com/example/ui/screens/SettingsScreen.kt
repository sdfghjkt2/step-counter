package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserSettingsEntity
import com.example.ui.theme.CyanTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import java.util.Locale

@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity,
    onUpdateGoal: (Int) -> Unit,
    onUpdateUserSettings: (strideMeters: Double, weightKg: Double, autoSync: Boolean) -> Unit,
    onRegenerateSampleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var goalSliderValue by remember(userSettings.dailyGoal) { mutableFloatStateOf(userSettings.dailyGoal.toFloat()) }
    var strideCmText by remember(userSettings.strideLengthMeters) { mutableStateOf((userSettings.strideLengthMeters * 100).toInt().toString()) }
    var weightKgText by remember(userSettings.weightKg) { mutableStateOf(userSettings.weightKg.toInt().toString()) }
    var autoSyncChecked by remember(userSettings.healthConnectAutoSync) { mutableStateOf(userSettings.healthConnectAutoSync) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Title Header
        item {
            Column {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Goal & Profile Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Daily Step Goal Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Daily Target Goal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${String.format(Locale.US, "%,d", goalSliderValue.toInt())} steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = goalSliderValue,
                        onValueChange = { goalSliderValue = it },
                        valueRange = 3000f..25000f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("daily_goal_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onUpdateGoal(goalSliderValue.toInt()) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_daily_goal_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Target", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Body Metrics & Calculation Parameters
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CyanTertiary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CyanTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Physical Profile & Calculations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stride Length (cm)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = strideCmText,
                                onValueChange = { strideCmText = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stride_cm_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Body Weight (kg)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weightKgText,
                                onValueChange = { weightKgText = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("weight_kg_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Sync Health Connect",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Background step synchronization",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = autoSyncChecked,
                            onCheckedChange = { autoSyncChecked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val strideM = (strideCmText.toDoubleOrNull() ?: 76.0) / 100.0
                            val weightK = weightKgText.toDoubleOrNull() ?: 70.0
                            onUpdateUserSettings(strideM, weightK, autoSyncChecked)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_settings_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanTertiary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Profile Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Demo Data Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEMO & REPORT TESTING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Populate the database with multi-week step history to immediately test the weekly performance report charts and comparisons.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onRegenerateSampleHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("regenerate_sample_history_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Multi-Week Step Records")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}
