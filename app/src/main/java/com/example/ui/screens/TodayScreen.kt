package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskEntity
import com.example.data.model.TimerState
import com.example.timer.TimerUiState
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.PriorityBadge
import com.example.ui.components.TaskItemRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    tasks: List<TaskEntity>,
    todayPriority: TaskEntity?,
    timerState: TimerUiState,
    todayFocusMinutes: Int,
    weeklyTasksCount: Int,
    onNavigateToFocus: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenQuickNote: () -> Unit,
    onOpenDailyReview: () -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusWithTask: (TaskEntity) -> Unit,
    onStartOrPauseTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val completionRatio = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    // Time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    val formattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    val motivationalLine = remember {
        val quotes = listOf(
            "Progress, not perfection.",
            "Small steps. Big progress.",
            "Focus on what truly matters.",
            "One mindful task at a time.",
            "Clarity precedes mastery."
        )
        quotes.random()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header with greeting and date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$formattedDate ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("today_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Active Focus Card or Quick Start Focus Banner
        item {
            TodayFocusCard(
                timerState = timerState,
                onNavigateToFocus = onNavigateToFocus,
                onStartOrPause = onStartOrPauseTimer
            )
        }

        // 3. Central Daily Progress Indicator
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("today_progress_card"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressRing(
                        progress = if (totalCount > 0) completionRatio else 0.05f,
                        modifier = Modifier.size(110.dp),
                        strokeWidth = 9.dp,
                        progressColor = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$completedCount/$totalCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "done today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = motivationalLine,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Four Quick Stat Tiles (as in user design mockup)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    title = "Today",
                    subtitle = "$totalCount tasks",
                    icon = Icons.Outlined.CalendarToday,
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "This Week",
                    subtitle = "$weeklyTasksCount tasks",
                    icon = Icons.Outlined.BarChart,
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    title = "Focus Time",
                    subtitle = "${todayFocusMinutes}m recorded",
                    icon = Icons.Outlined.Timer,
                    onClick = onNavigateToFocus,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    title = "Notes",
                    subtitle = "Quick thoughts",
                    icon = Icons.Outlined.EditNote,
                    onClick = onNavigateToNotes,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Quick Action Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionPill(
                    label = "+ Add Task",
                    icon = Icons.Default.Add,
                    isPrimary = true,
                    onClick = onOpenAddTask,
                    modifier = Modifier.weight(1.2f)
                )
                ActionPill(
                    label = "Quick Note",
                    icon = Icons.Outlined.NoteAdd,
                    isPrimary = false,
                    onClick = onOpenQuickNote,
                    modifier = Modifier.weight(1f)
                )
                ActionPill(
                    label = "Review",
                    icon = Icons.Outlined.RateReview,
                    isPrimary = false,
                    onClick = onOpenDailyReview,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        // 6. Today's Priority Card (if exists and pending)
        if (todayPriority != null && !todayPriority.isCompleted) {
            item {
                Text(
                    text = "Today's Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onEditTask(todayPriority) }
                        .testTag("today_priority_card"),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleTaskComplete(todayPriority) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            // Empty check circle
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = todayPriority.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (todayPriority.notes.isNotBlank()) {
                                Text(
                                    text = todayPriority.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PriorityBadge(priority = todayPriority.priority)
                                if (todayPriority.dueTime != null) {
                                    Text(
                                        text = todayPriority.dueTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onStartFocusWithTask(todayPriority) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Focus on Priority",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 7. Today's Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onNavigateToTasks) {
                    Text(
                        text = "View All ($totalCount)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your day is clear.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add what matters most today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskItemRow(
                    task = task,
                    onToggleComplete = { onToggleTaskComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onStartFocus = { onStartFocusWithTask(task) }
                )
            }
        }
    }
}

@Composable
fun TodayFocusCard(
    timerState: TimerUiState,
    onNavigateToFocus: () -> Unit,
    onStartOrPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = timerState.state == TimerState.RUNNING
    val isPaused = timerState.state == TimerState.PAUSED

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onNavigateToFocus() }
            .testTag("today_focus_card"),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isRunning) "Focus Session Active" else if (isPaused) "Focus Paused" else "Focus Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (timerState.attachedTask != null) timerState.attachedTask.title else "${timerState.formattedTime} remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onStartOrPause,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isRunning) "Pause" else if (isPaused) "Resume" else "Start",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun StatTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActionPill(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isPrimary) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
