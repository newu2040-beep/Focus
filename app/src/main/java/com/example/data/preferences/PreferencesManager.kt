package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AccentTheme
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val accentTheme: AccentTheme = AccentTheme.SAGE,
    val defaultFocusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val userName: String = "Focus Guide"
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("focus_guide_prefs", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val themeModeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val accentStr = prefs.getString("accent_theme", AccentTheme.SAGE.name) ?: AccentTheme.SAGE.name

        return UserPreferences(
            themeMode = try { AppThemeMode.valueOf(themeModeStr) } catch (_: Exception) { AppThemeMode.SYSTEM },
            accentTheme = try { AccentTheme.valueOf(accentStr) } catch (_: Exception) { AccentTheme.SAGE },
            defaultFocusMinutes = prefs.getInt("default_focus_minutes", 25),
            shortBreakMinutes = prefs.getInt("short_break_minutes", 5),
            longBreakMinutes = prefs.getInt("long_break_minutes", 15),
            autoStartBreaks = prefs.getBoolean("auto_start_breaks", false),
            autoStartFocus = prefs.getBoolean("auto_start_focus", false),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
            keepScreenAwake = prefs.getBoolean("keep_screen_awake", true),
            onboardingCompleted = prefs.getBoolean("onboarding_completed", false),
            userName = prefs.getString("user_name", "Focus Guide") ?: "Focus Guide"
        )
    }

    fun updateThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun updateAccentTheme(accent: AccentTheme) {
        prefs.edit().putString("accent_theme", accent.name).apply()
        _preferences.value = _preferences.value.copy(accentTheme = accent)
    }

    fun updateDefaultFocusMinutes(minutes: Int) {
        prefs.edit().putInt("default_focus_minutes", minutes).apply()
        _preferences.value = _preferences.value.copy(defaultFocusMinutes = minutes)
    }

    fun updateShortBreakMinutes(minutes: Int) {
        prefs.edit().putInt("short_break_minutes", minutes).apply()
        _preferences.value = _preferences.value.copy(shortBreakMinutes = minutes)
    }

    fun updateLongBreakMinutes(minutes: Int) {
        prefs.edit().putInt("long_break_minutes", minutes).apply()
        _preferences.value = _preferences.value.copy(longBreakMinutes = minutes)
    }

    fun updateAutoStartBreaks(enabled: Boolean) {
        prefs.edit().putBoolean("auto_start_breaks", enabled).apply()
        _preferences.value = _preferences.value.copy(autoStartBreaks = enabled)
    }

    fun updateAutoStartFocus(enabled: Boolean) {
        prefs.edit().putBoolean("auto_start_focus", enabled).apply()
        _preferences.value = _preferences.value.copy(autoStartFocus = enabled)
    }

    fun updateSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _preferences.value = _preferences.value.copy(soundEnabled = enabled)
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _preferences.value = _preferences.value.copy(vibrationEnabled = enabled)
    }

    fun updateKeepScreenAwake(enabled: Boolean) {
        prefs.edit().putBoolean("keep_screen_awake", enabled).apply()
        _preferences.value = _preferences.value.copy(keepScreenAwake = enabled)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _preferences.value = _preferences.value.copy(onboardingCompleted = completed)
    }

    fun updateUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _preferences.value = _preferences.value.copy(userName = name)
    }
}
