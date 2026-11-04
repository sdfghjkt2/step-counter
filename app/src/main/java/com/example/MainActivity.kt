package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HealthConnectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.WeeklyReportScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StepCounterTheme
import com.example.ui.viewmodel.StepViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Default.DirectionsWalk)
    object WeeklyReport : Screen("weekly_report", "Weekly Report", Icons.Default.BarChart)
    object HealthConnect : Screen("health_connect", "Health Connect", Icons.Default.HealthAndSafety)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StepCounterTheme(darkTheme = true) {
                StepCounterApp()
            }
        }
    }
}

@Composable
fun StepCounterApp() {
    val viewModel: StepViewModel = viewModel()
    val context = LocalContext.current

    val todayStep by viewModel.todayStep.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val weeklyPerformance by viewModel.weeklyPerformance.collectAsStateWithLifecycle()
    val selectedWeekStart by viewModel.selectedWeekStartDate.collectAsStateWithLifecycle()

    val healthAvailability by viewModel.healthConnectAvailability.collectAsStateWithLifecycle()
    val healthAuthorized by viewModel.healthConnectAuthorized.collectAsStateWithLifecycle()
    val healthSyncing by viewModel.healthConnectSyncing.collectAsStateWithLifecycle()
    val healthLogs by viewModel.healthConnectLogs.collectAsStateWithLifecycle()
    val liveWalkState by viewModel.liveWalkState.collectAsStateWithLifecycle()

    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Today) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Runtime Permission Launcher for Activity Recognition
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted for step sensor
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val screens = listOf(Screen.Today, Screen.WeeklyReport, Screen.HealthConnect, Screen.Settings)
                screens.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            is Screen.Today -> {
                TodayScreen(
                    dailyStep = todayStep,
                    todayLogs = todayLogs,
                    isHealthConnectSynced = todayStep.syncedWithHealthConnect,
                    isSyncing = healthSyncing,
                    liveWalkState = liveWalkState,
                    onAddSteps = { viewModel.addQuickSteps(it) },
                    onSetSteps = { viewModel.setTodaySteps(it) },
                    onSyncHealthConnect = { viewModel.syncHealthConnect() },
                    onNavigateToHealthConnect = { currentScreen = Screen.HealthConnect },
                    onStartWalkSimulation = { steps, dur -> viewModel.startWalkSimulation(steps, dur) },
                    onPauseWalkSimulation = { viewModel.pauseWalkSimulation() },
                    onResumeWalkSimulation = { viewModel.resumeWalkSimulation() },
                    onStopWalkSimulation = { viewModel.stopWalkSimulation() },
                    modifier = modifier
                )
            }
            is Screen.WeeklyReport -> {
                WeeklyReportScreen(
                    performance = weeklyPerformance,
                    selectedWeekStart = selectedWeekStart,
                    onSelectWeekOffset = { viewModel.selectWeekOffset(it) },
                    modifier = modifier
                )
            }
            is Screen.HealthConnect -> {
                HealthConnectScreen(
                    availability = healthAvailability,
                    isAuthorized = healthAuthorized,
                    isSyncing = healthSyncing,
                    syncLogs = healthLogs,
                    onToggleAuthorization = { viewModel.toggleHealthConnectAuth(it) },
                    onForceSync = { viewModel.syncHealthConnect() },
                    modifier = modifier
                )
            }
            is Screen.Settings -> {
                SettingsScreen(
                    userSettings = userSettings,
                    onUpdateGoal = { viewModel.updateDailyGoal(it) },
                    onUpdateUserSettings = { stride, weight, autoSync ->
                        viewModel.updateUserSettings(stride, weight, autoSync)
                    },
                    onRegenerateSampleHistory = { viewModel.regenerateSampleHistory() },
                    modifier = modifier
                )
            }
        }
    }
}
