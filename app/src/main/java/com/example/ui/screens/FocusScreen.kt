package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionMode
import com.example.data.model.TaskEntity
import com.example.data.model.TimerState
import com.example.timer.TimerUiState
import com.example.ui.components.CircularProgressRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    timerState: TimerUiState,
    tasks: List<TaskEntity>,
    onStartOrPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onEndSessionEarly: () -> Unit,
    onSelectMode: (SessionMode, Int?) -> Unit,
    onSetDurationMinutes: (Int) -> Unit,
    onAttachTask: (TaskEntity?) -> Unit,
    onDismissBreakSuggestion: () -> Unit,
    onStartBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTaskPicker by remember { mutableStateOf(false) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var customDurationInput by remember { mutableStateOf("30") }

    val isRunning = timerState.state == TimerState.RUNNING
    val isPaused = timerState.state == TimerState.PAUSED
    val isIdle = timerState.state == TimerState.IDLE || timerState.state == TimerState.COMPLETED

    val currentTotalMinutes = timerState.totalSeconds / 60

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Focus Timer",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Session #${timerState.sessionNumber} • ${timerState.mode.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Attached task button or chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showTaskPicker = true }
                    .testTag("focus_task_selector_button"),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timerState.attachedTask?.title?.take(14)?.let { "$it..." } ?: "Select Task",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Automatic Break Suggestion Banner
        AnimatedVisibility(visible = timerState.autoBreakSuggested) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("break_suggestion_card"),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Focus completed!",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Take 5 minutes to rest and recharge.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = onDismissBreakSuggestion) {
                            Text("Later", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Button(
                            onClick = onStartBreak,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Start Break")
                        }
                    }
                }
            }
        }

        // Mode Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(SessionMode.FOCUS, SessionMode.SHORT_BREAK, SessionMode.LONG_BREAK)
            modes.forEach { mode ->
                val selected = timerState.mode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { onSelectMode(mode, null) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Large Circular Timer UI
        Box(
            modifier = Modifier
                .size(270.dp)
                .testTag("focus_circular_timer"),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressRing(
                progress = timerState.progress,
                modifier = Modifier.size(260.dp),
                strokeWidth = 14.dp,
                progressColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timerState.formattedTime,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timerState.attachedTask?.title ?: timerState.mode.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    if (timerState.attachedTask != null) {
                        Text(
                            text = "Linked Task",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Primary Large Play / Pause / Resume Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onStartOrPause() }
                .testTag("focus_start_pause_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause Timer" else "Start Timer",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(38.dp)
            )
        }

        // Duration presets (15 min, 25 min, 45 min, 60 min, Custom)
        if (isIdle) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val presets = listOf(15, 25, 45, 60)
                presets.forEach { mins ->
                    val isSelected = currentTotalMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetDurationMinutes(mins) },
                        label = { Text("${mins}m") },
                        modifier = Modifier.padding(horizontal = 3.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                FilterChip(
                    selected = !presets.contains(currentTotalMinutes),
                    onClick = { showCustomDurationDialog = true },
                    label = { Text("Custom") },
                    modifier = Modifier.padding(horizontal = 3.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        } else {
            // Secondary in-session actions (Skip, End & Save, Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("focus_reset_button")
                ) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("focus_skip_button")
                ) {
                    Icon(imageVector = Icons.Outlined.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Skip")
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = onEndSessionEarly,
                    modifier = Modifier.testTag("focus_end_early_button")
                ) {
                    Icon(imageVector = Icons.Outlined.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("End & Save")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Task Picker Dialog
    if (showTaskPicker) {
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text("Assign Task to Focus") },
            text = {
                val pendingTasks = tasks.filter { !it.isCompleted }
                if (pendingTasks.isEmpty()) {
                    Text("No pending tasks. Create a task or focus freely.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Option: No specific task
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAttachTask(null)
                                    showTaskPicker = false
                                },
                            color = if (timerState.attachedTask == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "General Deep Focus (No task)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        pendingTasks.forEach { task ->
                            val isSelected = timerState.attachedTask?.id == task.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onAttachTask(task)
                                        showTaskPicker = false
                                    },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (task.isTodayPriority) {
                                        Text(
                                            text = "Priority",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTaskPicker = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Custom Duration Dialog
    if (showCustomDurationDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            title = { Text("Custom Focus Duration") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter duration in minutes (1 to 180):", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = customDurationInput,
                        onValueChange = { customDurationInput = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customDurationInput.toIntOrNull()?.coerceIn(1, 180) ?: 25
                        onSetDurationMinutes(mins)
                        showCustomDurationDialog = false
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
