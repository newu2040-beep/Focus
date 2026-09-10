package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.ui.theme.*

@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeftOffset = stroke / 2f

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset)
            )

            // Progress Arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset)
            )
        }
        content()
    }
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority) {
        Priority.LOW -> PriorityLowBg to PriorityLowText
        Priority.MEDIUM -> PriorityMediumBg to PriorityMediumText
        Priority.HIGH -> PriorityHighBg to PriorityHighText
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = bgColor
    ) {
        Text(
            text = priority.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

fun getCategoryIcon(category: TaskCategory): ImageVector {
    return when (category) {
        TaskCategory.WORK -> Icons.Outlined.WorkOutline
        TaskCategory.PERSONAL -> Icons.Outlined.PersonOutline
        TaskCategory.HEALTH -> Icons.Outlined.FavoriteBorder
        TaskCategory.STUDY -> Icons.Outlined.School
        TaskCategory.HOBBY -> Icons.Outlined.StarOutline
        TaskCategory.OTHER -> Icons.Outlined.MoreHoriz
    }
}

fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.WORK -> CategoryWork
        TaskCategory.PERSONAL -> CategoryPersonal
        TaskCategory.HEALTH -> CategoryHealth
        TaskCategory.STUDY -> CategoryStudy
        TaskCategory.HOBBY -> CategoryHobby
        TaskCategory.OTHER -> CategoryOther
    }
}

@Composable
fun CategoryBadge(category: TaskCategory, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = category.displayName,
            tint = getCategoryColor(category),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TaskItemRow(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkColor by animateColorAsState(
        targetValue = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "checkColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .testTag("task_item_${task.id}"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Checkbox
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) checkColor else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = checkColor,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 20.dp)
                    ) { onToggleComplete() }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PriorityBadge(priority = task.priority)
                    CategoryBadge(category = task.category)

                    if (!task.dueTime.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = task.dueTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action Quick Focus Button if not completed
            if (!task.isCompleted) {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("task_focus_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircleOutline,
                        contentDescription = "Start Focus on task",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("task_delete_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
