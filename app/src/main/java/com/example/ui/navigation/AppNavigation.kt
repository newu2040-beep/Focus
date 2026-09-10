package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskEntity
import com.example.ui.MainViewModel
import com.example.ui.screens.*

enum class AppTab(
    val title: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    val testTag: String
) {
    TODAY("Today", Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle, "nav_today"),
    FOCUS("Focus", Icons.Outlined.Timer, Icons.Filled.Timer, "nav_focus"),
    TASKS("Tasks", Icons.Outlined.FormatListBulleted, Icons.Filled.FormatListBulleted, "nav_tasks"),
    PROGRESS("Progress", Icons.Outlined.BarChart, Icons.Filled.BarChart, "nav_progress"),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Filled.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    onRequestNotificationPermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val todayPriority by viewModel.todayPriority.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val todayFocusMinutes by viewModel.todayFocusMinutes.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val todayReview by viewModel.todayReview.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    var currentTab by remember { mutableStateOf(AppTab.TODAY) }

    // Dialog & BottomSheet state
    var showAddEditTaskSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var showDailyReviewSheet by remember { mutableStateOf(false) }
    var showNotesSheet by remember { mutableStateOf(false) }
    var showExportReportSheet by remember { mutableStateOf(false) }

    if (!preferences.onboardingCompleted) {
        OnboardingScreen(
            onComplete = { viewModel.completeOnboarding() }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            FocusBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Crossfade(
                targetState = currentTab,
                label = "tab_crossfade"
            ) { tab ->
                when (tab) {
                    AppTab.TODAY -> TodayScreen(
                        tasks = tasks,
                        todayPriority = todayPriority,
                        timerState = timerState,
                        todayFocusMinutes = todayFocusMinutes,
                        weeklyTasksCount = tasks.size,
                        onNavigateToFocus = { currentTab = AppTab.FOCUS },
                        onNavigateToTasks = { currentTab = AppTab.TASKS },
                        onNavigateToNotes = { showNotesSheet = true },
                        onNavigateToSettings = { currentTab = AppTab.SETTINGS },
                        onOpenAddTask = {
                            taskToEdit = null
                            showAddEditTaskSheet = true
                        },
                        onOpenQuickNote = { showNotesSheet = true },
                        onOpenDailyReview = { showDailyReviewSheet = true },
                        onToggleTaskComplete = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = {
                            taskToEdit = it
                            showAddEditTaskSheet = true
                        },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onStartFocusWithTask = { task ->
                            viewModel.attachTaskToTimer(task)
                            currentTab = AppTab.FOCUS
                            viewModel.startOrPauseTimer()
                        },
                        onStartOrPauseTimer = { viewModel.startOrPauseTimer() }
                    )
                    AppTab.FOCUS -> FocusScreen(
                        timerState = timerState,
                        tasks = tasks,
                        onStartOrPause = { viewModel.startOrPauseTimer() },
                        onReset = { viewModel.resetTimer() },
                        onSkip = { viewModel.skipTimer() },
                        onEndSessionEarly = { viewModel.endTimerEarly() },
                        onSelectMode = { mode, mins -> viewModel.setTimerMode(mode, mins) },
                        onSetDurationMinutes = { viewModel.setTimerDuration(it) },
                        onAttachTask = { viewModel.attachTaskToTimer(it) },
                        onDismissBreakSuggestion = { viewModel.dismissBreakSuggestion() },
                        onStartBreak = { viewModel.startBreakFromSuggestion() }
                    )
                    AppTab.TASKS -> TasksScreen(
                        tasks = tasks,
                        onToggleTaskComplete = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = {
                            taskToEdit = it
                            showAddEditTaskSheet = true
                        },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onStartFocusWithTask = { task ->
                            viewModel.attachTaskToTimer(task)
                            currentTab = AppTab.FOCUS
                            viewModel.startOrPauseTimer()
                        },
                        onOpenAddTask = {
                            taskToEdit = null
                            showAddEditTaskSheet = true
                        },
                        onClearCompleted = { viewModel.clearCompletedTasks() }
                    )
                    AppTab.PROGRESS -> ProgressScreen(
                        tasks = tasks,
                        sessions = sessions,
                        onOpenDailyReview = { showDailyReviewSheet = true },
                        dailyReview = todayReview
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        preferences = preferences,
                        onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                        onUpdateAccentTheme = { viewModel.updateAccentTheme(it) },
                        onUpdateDefaultFocusMinutes = { viewModel.updateDefaultFocusMinutes(it) },
                        onUpdateShortBreakMinutes = { viewModel.updateShortBreakMinutes(it) },
                        onUpdateLongBreakMinutes = { viewModel.updateLongBreakMinutes(it) },
                        onUpdateSoundEnabled = { viewModel.updateSoundEnabled(it) },
                        onUpdateVibrationEnabled = { viewModel.updateVibrationEnabled(it) },
                        onUpdateKeepScreenAwake = { viewModel.updateKeepScreenAwake(it) },
                        onExportData = { viewModel.exportDataJson() },
                        onImportData = { viewModel.importDataJson(it) },
                        onClearAllData = { viewModel.clearAllData() },
                        onOpenExportReport = { showExportReportSheet = true },
                        onRequestNotificationPermission = onRequestNotificationPermission
                    )
                }
            }
        }
    }

    // Add / Edit Task BottomSheet
    if (showAddEditTaskSheet) {
        AddEditTaskBottomSheet(
            taskToEdit = taskToEdit,
            onSaveTask = { title, notes, priority, category, dueDate, dueTime, estimatedMinutes, isTodayPriority ->
                if (taskToEdit != null) {
                    viewModel.updateTask(
                        taskToEdit!!.copy(
                            title = title,
                            notes = notes,
                            priority = priority,
                            category = category,
                            dueDate = dueDate,
                            dueTime = dueTime,
                            estimatedMinutes = estimatedMinutes,
                            isTodayPriority = isTodayPriority
                        )
                    )
                } else {
                    viewModel.createTask(
                        title = title,
                        notes = notes,
                        priority = priority,
                        category = category,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        estimatedMinutes = estimatedMinutes,
                        isTodayPriority = isTodayPriority
                    )
                }
            },
            onDismiss = {
                showAddEditTaskSheet = false
                taskToEdit = null
            }
        )
    }

    // Daily Review Sheet
    if (showDailyReviewSheet) {
        DailyReviewBottomSheet(
            existingReview = todayReview,
            onSaveReview = { rating, whatWentWell, distractions, tomorrowPriority ->
                viewModel.saveDailyReview(rating, whatWentWell, distractions, tomorrowPriority)
            },
            onDismiss = { showDailyReviewSheet = false }
        )
    }

    // Notes Sheet
    if (showNotesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotesSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.9f)) {
                NotesScreen(
                    notes = notes,
                    onSaveNote = { viewModel.saveNote(it) },
                    onDeleteNote = { viewModel.deleteNote(it) }
                )
            }
        }
    }

    // Export Report Bottom Sheet
    if (showExportReportSheet) {
        ExportReportBottomSheet(
            tasks = tasks,
            sessions = sessions,
            review = todayReview,
            onDismiss = { showExportReportSheet = false }
        )
    }
}

@Composable
fun FocusBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.values().forEach { tab ->
                val selected = currentTab == tab
                val icon = if (selected) tab.filledIcon else tab.outlinedIcon

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
