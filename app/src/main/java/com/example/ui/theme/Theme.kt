package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AccentTheme
import com.example.data.model.AppThemeMode

fun buildFocusColorScheme(darkTheme: Boolean, accent: AccentTheme): ColorScheme {
    return if (darkTheme) {
        when (accent) {
            AccentTheme.SAGE -> darkColorScheme(
                primary = SagePrimaryDark,
                onPrimary = SageOnPrimaryDark,
                primaryContainer = SagePrimaryContainerDark,
                onPrimaryContainer = SageOnPrimaryContainerDark,
                secondary = SageSecondaryDark,
                background = SageBackgroundDark,
                surface = SageSurfaceDark,
                surfaceVariant = SageSurfaceVariantDark,
                outline = SageOutlineDark
            )
            AccentTheme.OCEAN -> darkColorScheme(
                primary = OceanPrimaryDark,
                onPrimary = Color(0xFF00344A),
                primaryContainer = OceanPrimaryContainerDark,
                onPrimaryContainer = Color(0xFFBCE6FF),
                background = Color(0xFF0F1417),
                surface = Color(0xFF161D22),
                surfaceVariant = Color(0xFF222B32)
            )
            AccentTheme.AMBER -> darkColorScheme(
                primary = AmberPrimaryDark,
                onPrimary = Color(0xFF452B04),
                primaryContainer = AmberPrimaryContainerDark,
                onPrimaryContainer = Color(0xFFFFDDB3),
                background = Color(0xFF151310),
                surface = Color(0xFF1E1A15),
                surfaceVariant = Color(0xFF2C2720)
            )
            AccentTheme.ROSE -> darkColorScheme(
                primary = RosePrimaryDark,
                onPrimary = Color(0xFF4C1821),
                primaryContainer = RosePrimaryContainerDark,
                onPrimaryContainer = Color(0xFFFFD9DD),
                background = Color(0xFF151012),
                surface = Color(0xFF1F171A),
                surfaceVariant = Color(0xFF2D2226)
            )
        }
    } else {
        when (accent) {
            AccentTheme.SAGE -> lightColorScheme(
                primary = SagePrimaryLight,
                onPrimary = SageOnPrimaryLight,
                primaryContainer = SagePrimaryContainerLight,
                onPrimaryContainer = SageOnPrimaryContainerLight,
                secondary = SageSecondaryLight,
                background = SageBackgroundLight,
                surface = SageSurfaceLight,
                surfaceVariant = SageSurfaceVariantLight,
                outline = SageOutlineLight
            )
            AccentTheme.OCEAN -> lightColorScheme(
                primary = OceanPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = OceanPrimaryContainerLight,
                onPrimaryContainer = Color(0xFF001F2E),
                background = Color(0xFFF7FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFE8EFF3)
            )
            AccentTheme.AMBER -> lightColorScheme(
                primary = AmberPrimaryLight,
                onPrimary = Color.White,
                primaryContainer = AmberPrimaryContainerLight,
                onPrimaryContainer = Color(0xFF2C1700),
                background = Color(0xFFFAF9F6),
                surface = Color.White,
                surfaceVariant = Color(0xFFF3EFE9)
            )
            AccentTheme.ROSE -> lightColorScheme(
                primary = RosePrimaryLight,
                onPrimary = Color.White,
                primaryContainer = RosePrimaryContainerLight,
                onPrimaryContainer = Color(0xFF330911),
                background = Color(0xFFFAF7F8),
                surface = Color.White,
                surfaceVariant = Color(0xFFF4EBEC)
            )
        }
    }
}

@Composable
fun FocusGuideTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    accentTheme: AccentTheme = AccentTheme.SAGE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = buildFocusColorScheme(isDark, accentTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
