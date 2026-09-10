package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyReviewEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.TaskEntity
import com.example.util.ReportFormat
import com.example.util.ReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportBottomSheet(
    tasks: List<TaskEntity>,
    sessions: List<FocusSessionEntity>,
    review: DailyReviewEntity?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }
    var isExporting by remember { mutableStateOf(false) }

    // SAF Document Creator launcher
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType)
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ReportGenerator.writeReport(outputStream, selectedFormat, tasks, sessions, review)
                }
                Toast.makeText(context, "Report saved successfully!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Export Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Generate and share productivity reports",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_export_report_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Info Badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReportStatItem(
                        value = "${tasks.size}",
                        label = "Tasks",
                        icon = Icons.Outlined.CheckCircle
                    )
                    Divider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ReportStatItem(
                        value = "${sessions.sumOf { it.completedSeconds } / 60}m",
                        label = "Focus Time",
                        icon = Icons.Outlined.Timer
                    )
                    Divider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ReportStatItem(
                        value = "${sessions.size}",
                        label = "Sessions",
                        icon = Icons.Outlined.BarChart
                    )
                }
            }

            Text(
                text = "Select File Format",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Format Selection Options
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormatOptionCard(
                    format = ReportFormat.PDF,
                    title = "PDF Document (.pdf)",
                    description = "Visual report with tables, executive KPIs & review notes",
                    icon = Icons.Outlined.PictureAsPdf,
                    isSelected = selectedFormat == ReportFormat.PDF,
                    onClick = { selectedFormat = ReportFormat.PDF }
                )

                FormatOptionCard(
                    format = ReportFormat.CSV,
                    title = "CSV Spreadsheet (.csv)",
                    description = "Structured rows for Excel, Google Sheets or analytics",
                    icon = Icons.Outlined.TableChart,
                    isSelected = selectedFormat == ReportFormat.CSV,
                    onClick = { selectedFormat = ReportFormat.CSV }
                )

                FormatOptionCard(
                    format = ReportFormat.TXT,
                    title = "Plain Text (.txt)",
                    description = "Clean markdown-style ASCII report for easy reading",
                    icon = Icons.Outlined.Description,
                    isSelected = selectedFormat == ReportFormat.TXT,
                    onClick = { selectedFormat = ReportFormat.TXT }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: Share File & Save to Device
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share button
                OutlinedButton(
                    onClick = {
                        isExporting = true
                        try {
                            val file = ReportGenerator.generateTempReport(
                                context,
                                selectedFormat,
                                tasks,
                                sessions,
                                review
                            )
                            ReportGenerator.shareReport(
                                context,
                                file,
                                selectedFormat,
                                chooserTitle = "Share ${selectedFormat.displayName}"
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error sharing: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isExporting = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("share_report_button"),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isExporting
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Share")
                }

                // Save to Storage button
                Button(
                    onClick = {
                        val fileName = ReportGenerator.getDefaultFileName(selectedFormat)
                        createDocumentLauncher.launch(fileName)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_report_button"),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isExporting
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save to File")
                }
            }
        }
    }
}

@Composable
private fun FormatOptionCard(
    format: ReportFormat,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("format_option_${format.extension}"),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ReportStatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
