package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.model.TimerState
import com.example.ui.MainViewModel
import com.example.ui.components.PermissionRequestSheet
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.FocusGuideTheme
import com.example.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.preferences.collectAsState()
            val timerState by viewModel.timerState.collectAsState()

            var showPermissionRationaleSheet by remember { mutableStateOf(false) }

            // Keep screen on logic during active focus
            LaunchedEffect(preferences.keepScreenAwake, timerState.state) {
                if (preferences.keepScreenAwake && timerState.state == TimerState.RUNNING) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Notification permission request for Android 13+
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Handled gracefully
                showPermissionRationaleSheet = false
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Proactively ask with rationale
                        showPermissionRationaleSheet = true
                    }
                }
            }

            FocusGuideTheme(
                themeMode = preferences.themeMode,
                accentTheme = preferences.accentTheme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        viewModel = viewModel,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                PermissionHelper.openAppNotificationSettings(this@MainActivity)
                            }
                        }
                    )

                    if (showPermissionRationaleSheet) {
                        PermissionRequestSheet(
                            onGrantClick = {
                                showPermissionRationaleSheet = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    PermissionHelper.openAppNotificationSettings(this@MainActivity)
                                }
                            },
                            onDismiss = {
                                showPermissionRationaleSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}
