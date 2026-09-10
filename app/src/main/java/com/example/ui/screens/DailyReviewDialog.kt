package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyReviewEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewBottomSheet(
    existingReview: DailyReviewEntity?,
    onSaveReview: (rating: Int, whatWentWell: String, distractions: String, tomorrowPriority: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(existingReview?.rating ?: 4) }
    var whatWentWell by remember { mutableStateOf(existingReview?.whatWentWell ?: "") }
    var distractions by remember { mutableStateOf(existingReview?.distractions ?: "") }
    var tomorrowPriority by remember { mutableStateOf(existingReview?.tomorrowPriority ?: "") }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Review",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Mood / Day Rating (1 to 5 Stars)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "How did today feel?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Rating $i",
                                tint = if (i <= rating) Color(0xFFFFA000) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Prompt 1: What went well
            OutlinedTextField(
                value = whatWentWell,
                onValueChange = { whatWentWell = it },
                label = { Text("What went well today?") },
                placeholder = { Text("Wins, completed milestones, or focused flow state...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Prompt 2: What caused friction
            OutlinedTextField(
                value = distractions,
                onValueChange = { distractions = it },
                label = { Text("What caused distraction or friction?") },
                placeholder = { Text("Interruptions, fatigue, unclear priorities...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Prompt 3: Tomorrow's priority
            OutlinedTextField(
                value = tomorrowPriority,
                onValueChange = { tomorrowPriority = it },
                label = { Text("Top priority for tomorrow") },
                placeholder = { Text("One thing that will make tomorrow successful...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Save Review Button
            Button(
                onClick = {
                    onSaveReview(rating, whatWentWell.trim(), distractions.trim(), tomorrowPriority.trim())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_daily_review_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Save Daily Reflection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
