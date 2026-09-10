package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccentTheme
import com.example.data.model.AppThemeMode
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.*
import com.example.util.PermissionHelper

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onUpdateThemeMode: (AppThemeMode) -> Unit,
    onUpdateAccentTheme: (AccentTheme) -> Unit,
    onUpdateDefaultFocusMinutes: (Int) -> Unit,
    onUpdateShortBreakMinutes: (Int) -> Unit,
    onUpdateLongBreakMinutes: (Int) -> Unit,
    onUpdateSoundEnabled: (Boolean) -> Unit,
    onUpdateVibrationEnabled: (Boolean) -> Unit,
    onUpdateKeepScreenAwake: (Boolean) -> Unit,
    onExportData: () -> String,
    onImportData: (String) -> Unit,
    onClearAllData: () -> Unit,
    onOpenExportReport: (() -> Unit)? = null,
    onRequestNotificationPermission: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. Appearance Section
        item {
            SettingsCard(title = "Appearance") {
                // Theme Mode (System / Light / Dark)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppThemeMode.values().forEach { mode ->
                            val isSelected = preferences.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { onUpdateThemeMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accent Color
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AccentTheme.values().forEach { accent ->
                            val isSelected = preferences.accentTheme == accent
                            val color = when (accent) {
                                AccentTheme.SAGE -> SagePrimaryLight
                                AccentTheme.OCEAN -> OceanPrimaryLight
                                AccentTheme.AMBER -> AmberPrimaryLight
                                AccentTheme.ROSE -> RosePrimaryLight
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onUpdateAccentTheme(accent) }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = accent.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Timer Preferences
        item {
            SettingsCard(title = "Timer Defaults") {
                // Focus duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Default Focus Length",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 25, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = preferences.defaultFocusMinutes == mins,
                                onClick = { onUpdateDefaultFocusMinutes(mins) },
                                label = { Text("${mins}m") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 6.dp))

                // Short break
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Short Break",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10).forEach { mins ->
                            FilterChip(
                                selected = preferences.shortBreakMinutes == mins,
                                onClick = { onUpdateShortBreakMinutes(mins) },
                                label = { Text("${mins}m") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 6.dp))

                // Long break
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Long Break",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 20).forEach { mins ->
                            FilterChip(
                                selected = preferences.longBreakMinutes == mins,
                                onClick = { onUpdateLongBreakMinutes(mins) },
                                label = { Text("${mins}m") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Sound, Vibration & Display
        item {
            SettingsCard(title = "Alerts & Display") {
                SettingSwitchRow(
                    title = "Sound Effects",
                    subtitle = "Gentle chime when focus session or break ends",
                    checked = preferences.soundEnabled,
                    onCheckedChange = onUpdateSoundEnabled
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingSwitchRow(
                    title = "Vibration & Haptics",
                    subtitle = "Subtle tactile feedback for buttons and timer completion",
                    checked = preferences.vibrationEnabled,
                    onCheckedChange = onUpdateVibrationEnabled
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingSwitchRow(
                    title = "Keep Screen On",
                    subtitle = "Prevent display from sleeping during active focus",
                    checked = preferences.keepScreenAwake,
                    onCheckedChange = onUpdateKeepScreenAwake
                )
            }
        }

        // 4. Permissions & System Access Section
        item {
            SettingsCard(title = "Permissions & System Access") {
                val isNotificationGranted = PermissionHelper.isNotificationPermissionGranted(context)
                val isBatteryIgnoring = PermissionHelper.isIgnoringBatteryOptimizations(context)

                // Notification Permission Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isNotificationGranted) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isNotificationGranted) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                tint = if (isNotificationGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Focus Notifications",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isNotificationGranted) "Enabled (Chimes on timer finish)" else "Disabled (Tap to enable alerts)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isNotificationGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (isNotificationGranted) {
                        FilledTonalButton(
                            onClick = { PermissionHelper.openAppNotificationSettings(context) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Manage", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (onRequestNotificationPermission != null) {
                                    onRequestNotificationPermission()
                                } else {
                                    PermissionHelper.openAppNotificationSettings(context)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Enable", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                // Battery Optimization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BatterySaver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Background Timers",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBatteryIgnoring) "Unrestricted (Smooth background counting)" else "Standard (Can be optimized for zero lag)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { PermissionHelper.openBatteryOptimizationSettings(context) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Battery", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                // Vibration Hardware
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Haptics & Vibration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hardware vibration active on device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Data & Privacy (Offline-First Guarantee & Export)
        item {
            SettingsCard(title = "Data & Privacy") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "100% Offline-First",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your tasks, notes, and sessions never leave this device. No trackers, cloud dependencies, or account requirements.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Export Report Button
                if (onOpenExportReport != null) {
                    FilledTonalButton(
                        onClick = onOpenExportReport,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Productivity Report (PDF, CSV, TXT)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exportedJsonText = onExportData()
                            showExportDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup")
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = { showClearDataDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Outlined.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All App Data")
                }
            }
        }

        // 5. About Section
        item {
            SettingsCard(title = "About Focus Guide") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Focus Guide v1.0.0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "A minimalist focus and daily priority companion designed to help you plan with clarity, do deep focused work, and reflect without distraction.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Reset All Data?") },
            text = { Text("This will permanently remove all tasks, focus sessions, notes, and reviews. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "All data has been reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Data Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Offline Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Copy your exported backup JSON string:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJsonText))
                        Toast.makeText(context, "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Data Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore Offline Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste your backup JSON string below:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Paste JSON here...") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            onImportData(importJsonText.trim())
                            Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                        }
                        showImportDialog = false
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
