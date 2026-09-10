package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FocusDatabase
import com.example.data.model.AccentTheme
import com.example.data.model.AppThemeMode
import com.example.data.model.DailyReviewEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.NoteEntity
import com.example.data.model.Priority
import com.example.data.model.SessionMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.UserPreferences
import com.example.data.repository.FocusRepository
import com.example.timer.FocusTimerEngine
import com.example.timer.TimerUiState
import com.example.util.HapticHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database: FocusDatabase = FocusDatabase.getDatabase(application, viewModelScope)
    private val repository: FocusRepository = FocusRepository(
        database.taskDao(),
        database.focusSessionDao(),
        database.noteDao(),
        database.dailyReviewDao()
    )

    private val preferencesManager: PreferencesManager = PreferencesManager(application)
    private val notificationHelper: NotificationHelper = NotificationHelper(application)
    private val hapticHelper: HapticHelper = HapticHelper(application)

    val timerEngine: FocusTimerEngine = FocusTimerEngine(
        repository = repository,
        notificationHelper = notificationHelper,
        hapticHelper = hapticHelper,
        scope = viewModelScope
    )

    val preferences: StateFlow<UserPreferences> = preferencesManager.preferences

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayPriority: StateFlow<TaskEntity?> = repository.todayPriority.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val sessions: StateFlow<List<FocusSessionEntity>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todaySessions: StateFlow<List<FocusSessionEntity>> = repository.getSessionsToday().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayFocusMinutes: StateFlow<Int> = todaySessions.map { list ->
        list.sumOf { it.completedSeconds } / 60
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayReview: StateFlow<DailyReviewEntity?> = repository.getReviewForToday().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val timerState: StateFlow<TimerUiState> = timerEngine.uiState

    // Task Actions
    fun createTask(
        title: String,
        notes: String = "",
        priority: Priority = Priority.MEDIUM,
        category: TaskCategory = TaskCategory.WORK,
        dueDate: Long? = null,
        dueTime: String? = null,
        estimatedMinutes: Int = 25,
        isTodayPriority: Boolean = false
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    notes = notes,
                    priority = priority,
                    category = category,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    estimatedMinutes = estimatedMinutes,
                    isTodayPriority = isTodayPriority
                )
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            hapticHelper.playClickHaptic(preferences.value.vibrationEnabled)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
        }
    }

    // Timer Actions
    fun startOrPauseTimer() {
        val prefs = preferences.value
        timerEngine.startOrResume(
            soundEnabled = prefs.soundEnabled,
            vibrationEnabled = prefs.vibrationEnabled
        )
    }

    fun resetTimer() {
        timerEngine.reset()
    }

    fun skipTimer() {
        val prefs = preferences.value
        timerEngine.skip(
            soundEnabled = prefs.soundEnabled,
            vibrationEnabled = prefs.vibrationEnabled
        )
    }

    fun endTimerEarly() {
        timerEngine.endEarlyAndSave()
    }

    fun setTimerMode(mode: SessionMode, durationMinutes: Int? = null) {
        val defaultMins = when (mode) {
            SessionMode.FOCUS -> preferences.value.defaultFocusMinutes
            SessionMode.SHORT_BREAK -> preferences.value.shortBreakMinutes
            SessionMode.LONG_BREAK -> preferences.value.longBreakMinutes
            SessionMode.CUSTOM -> durationMinutes ?: 30
        }
        timerEngine.setMode(mode, durationMinutes ?: defaultMins)
    }

    fun setTimerDuration(minutes: Int) {
        timerEngine.setDuration(minutes)
    }

    fun attachTaskToTimer(task: TaskEntity?) {
        timerEngine.attachTask(task)
    }

    fun dismissBreakSuggestion() {
        timerEngine.dismissBreakSuggestion()
    }

    fun startBreakFromSuggestion() {
        setTimerMode(SessionMode.SHORT_BREAK, preferences.value.shortBreakMinutes)
        startOrPauseTimer()
    }

    // Notes Actions
    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Daily Review Actions
    fun saveDailyReview(rating: Int, whatWentWell: String, distractions: String, tomorrowPriority: String) {
        viewModelScope.launch {
            val dateKey = repository.getTodayDateKey()
            repository.saveDailyReview(
                DailyReviewEntity(
                    dateKey = dateKey,
                    rating = rating,
                    whatWentWell = whatWentWell,
                    distractions = distractions,
                    tomorrowPriority = tomorrowPriority,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Preferences
    fun updateThemeMode(mode: AppThemeMode) {
        preferencesManager.updateThemeMode(mode)
    }

    fun updateAccentTheme(accent: AccentTheme) {
        preferencesManager.updateAccentTheme(accent)
    }

    fun updateDefaultFocusMinutes(minutes: Int) {
        preferencesManager.updateDefaultFocusMinutes(minutes)
        if (timerState.value.mode == SessionMode.FOCUS) {
            timerEngine.setDuration(minutes)
        }
    }

    fun updateShortBreakMinutes(minutes: Int) {
        preferencesManager.updateShortBreakMinutes(minutes)
    }

    fun updateLongBreakMinutes(minutes: Int) {
        preferencesManager.updateLongBreakMinutes(minutes)
    }

    fun updateSoundEnabled(enabled: Boolean) {
        preferencesManager.updateSoundEnabled(enabled)
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        preferencesManager.updateVibrationEnabled(enabled)
    }

    fun updateKeepScreenAwake(enabled: Boolean) {
        preferencesManager.updateKeepScreenAwake(enabled)
    }

    fun completeOnboarding() {
        preferencesManager.setOnboardingCompleted(true)
    }

    // Export & Import
    fun exportDataJson(): String = runBlocking {
        repository.exportDataJson(tasks.value, sessions.value, notes.value)
    }

    fun importDataJson(jsonString: String) {
        viewModelScope.launch {
            repository.importDataJson(jsonString)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            timerEngine.reset()
        }
    }
}
