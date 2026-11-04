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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.health.HealthConnectAvailability
import com.example.data.health.HealthConnectSyncLog
import com.example.ui.theme.CyanTertiary
import com.example.ui.theme.EmeraldPrimary
import java.util.Locale

@Composable
fun HealthConnectScreen(
    availability: HealthConnectAvailability,
    isAuthorized: Boolean,
    isSyncing: Boolean,
    syncLogs: List<HealthConnectSyncLog>,
    onToggleAuthorization: (Boolean) -> Unit,
    onForceSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Title
        item {
            Column {
                Text(
                    text = "GOOGLE INTEGRATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Health Connect Sync",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Connection Status Card
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = "Health Connect",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Health Connect",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isAuthorized) "Connected & Authorized" else "Sync Suspended",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isAuthorized) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isAuthorized,
                            onCheckedChange = { onToggleAuthorization(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("health_connect_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Syncs your daily step counts, total distance, and active calorie expenditure securely with Google Health Connect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onForceSync,
                        enabled = isAuthorized && !isSyncing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("force_sync_now_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Force Sync",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSyncing) "Syncing with Google Health..." else "Force Manual Sync Now",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Permissions & Data Types Managed
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HEALTH CONNECT DATA PERMISSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanTertiary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PermissionItemRow(
                        title = "Steps (Read & Write)",
                        subtitle = "Syncs daily step totals to Google Health",
                        icon = Icons.Default.DirectionsWalk,
                        isGranted = isAuthorized
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionItemRow(
                        title = "Distance (Read & Write)",
                        subtitle = "Calculated km/miles walking distance",
                        icon = Icons.Default.DirectionsRun,
                        isGranted = isAuthorized
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionItemRow(
                        title = "Active Calories (Read & Write)",
                        subtitle = "Estimated active caloric expenditure",
                        icon = Icons.Default.LocalFireDepartment,
                        isGranted = isAuthorized
                    )
                }
            }
        }

        // Recent Sync Activity Logs
        item {
            Text(
                text = "RECENT SYNC LOGS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
        }

        if (syncLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No Health Connect sync activity recorded yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(syncLogs) { log ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (log.status == HealthConnectSyncLog.SyncStatus.SUCCESS) Icons.Default.CheckCircle else Icons.Default.Sync,
                                contentDescription = null,
                                tint = if (log.status == HealthConnectSyncLog.SyncStatus.SUCCESS) EmeraldPrimary else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = log.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = log.dateString,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (log.stepsSynced > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%,d", log.stepsSynced)} steps",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = if (isGranted) EmeraldPrimary.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isGranted) "Active" else "Inactive",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isGranted) EmeraldPrimary else Color(0xFFEF4444),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
