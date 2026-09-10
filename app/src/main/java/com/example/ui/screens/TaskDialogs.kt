package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskBottomSheet(
    taskToEdit: TaskEntity?,
    onSaveTask: (
        title: String,
        notes: String,
        priority: Priority,
        category: TaskCategory,
        dueDate: Long?,
        dueTime: String?,
        estimatedMinutes: Int,
        isTodayPriority: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = taskToEdit != null

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var notes by remember { mutableStateOf(taskToEdit?.notes ?: "") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(taskToEdit?.category ?: TaskCategory.WORK) }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate) }
    var dueTime by remember { mutableStateOf(taskToEdit?.dueTime ?: "12:00 PM") }
    var estimatedMinutes by remember { mutableStateOf(taskToEdit?.estimatedMinutes ?: 25) }
    var isTodayPriority by remember { mutableStateOf(taskToEdit?.isTodayPriority ?: false) }

    var isTitleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Task" else "Add Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Task title input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (isTitleError && it.isNotBlank()) isTitleError = false
                },
                label = { Text("Task title") },
                placeholder = { Text("e.g. Read a book, Finish proposal") },
                singleLine = true,
                isError = isTitleError,
                supportingText = if (isTitleError) {
                    { Text("Please enter a task title") }
                } else null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            // Priority Selector Pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { p ->
                        val isSelected = priority == p
                        val (bg, textColor) = when (p) {
                            Priority.LOW -> PriorityLowBg to PriorityLowText
                            Priority.MEDIUM -> PriorityMediumBg to PriorityMediumText
                            Priority.HIGH -> PriorityHighBg to PriorityHighText
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) textColor else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { priority = p },
                            color = bg
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.displayName,
                                    color = textColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategory.values().take(3).forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategory.values().drop(3).forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Due Time Selection Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val amPm = if (hourOfDay < 12) "AM" else "PM"
                                val formattedHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                dueTime = "%02d:%02d %s".format(formattedHour, minute, amPm)
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Due Time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = dueTime,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Estimated Focus Duration
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Estimated Focus Duration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val durations = listOf(15, 25, 45, 60)
                    durations.forEach { d ->
                        val isSelected = estimatedMinutes == d
                        FilterChip(
                            selected = isSelected,
                            onClick = { estimatedMinutes = d },
                            label = { Text("${d}m") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Notes / Description
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Add key details or thoughts...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Set as Today's Priority Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Priority",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Highlight as the primary focus today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isTodayPriority,
                    onCheckedChange = { isTodayPriority = it }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Action: Save Task Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                        return@Button
                    }
                    onSaveTask(
                        title.trim(),
                        notes.trim(),
                        priority,
                        category,
                        dueDate ?: System.currentTimeMillis(),
                        dueTime,
                        estimatedMinutes,
                        isTodayPriority
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isEdit) "Update Task" else "Save Task",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
