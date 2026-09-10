package com.example.timer

import com.example.data.model.SessionMode
import com.example.data.model.TaskEntity
import com.example.data.model.TimerState
import com.example.data.repository.FocusRepository
import com.example.util.HapticHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val state: TimerState = TimerState.IDLE,
    val mode: SessionMode = SessionMode.FOCUS,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val sessionNumber: Int = 1,
    val attachedTask: TaskEntity? = null,
    val autoBreakSuggested: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat() else 0f

    val formattedTime: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

class FocusTimerEngine(
    private val repository: FocusRepository,
    private val notificationHelper: NotificationHelper,
    private val hapticHelper: HapticHelper,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var endTimestamp: Long = 0L

    fun setMode(mode: SessionMode, durationMinutes: Int? = null) {
        val minutes = durationMinutes ?: mode.defaultMinutes
        val totalSec = minutes * 60
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            state = TimerState.IDLE,
            mode = mode,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            autoBreakSuggested = false
        )
    }

    fun setDuration(minutes: Int) {
        val totalSec = minutes * 60
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            state = TimerState.IDLE,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            autoBreakSuggested = false
        )
    }

    fun attachTask(task: TaskEntity?) {
        _uiState.value = _uiState.value.copy(attachedTask = task)
    }

    fun startOrResume(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        val current = _uiState.value
        hapticHelper.playClickHaptic(vibrationEnabled)

        when (current.state) {
            TimerState.IDLE, TimerState.PAUSED, TimerState.COMPLETED -> {
                endTimestamp = System.currentTimeMillis() + (current.remainingSeconds * 1000L)
                _uiState.value = current.copy(state = TimerState.RUNNING, autoBreakSuggested = false)
                startTicking(soundEnabled, vibrationEnabled)
            }
            TimerState.RUNNING -> {
                // Pause
                pause(vibrationEnabled)
            }
        }
    }

    fun pause(vibrationEnabled: Boolean = true) {
        hapticHelper.playClickHaptic(vibrationEnabled)
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(state = TimerState.PAUSED)
    }

    fun reset() {
        timerJob?.cancel()
        val total = _uiState.value.totalSeconds
        _uiState.value = _uiState.value.copy(
            state = TimerState.IDLE,
            remainingSeconds = total,
            autoBreakSuggested = false
        )
    }

    fun skip(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        hapticHelper.playClickHaptic(vibrationEnabled)
        timerJob?.cancel()
        completeSession(isSkip = true, soundEnabled = soundEnabled, vibrationEnabled = vibrationEnabled)
    }

    fun endEarlyAndSave() {
        val current = _uiState.value
        val elapsed = current.totalSeconds - current.remainingSeconds
        if (elapsed >= 60) { // save if focused for at least 1 minute
            scope.launch {
                repository.recordSession(
                    sessionType = current.mode,
                    durationMinutes = current.totalSeconds / 60,
                    completedSeconds = elapsed,
                    taskId = current.attachedTask?.id,
                    taskTitle = current.attachedTask?.title
                )
            }
        }
        reset()
    }

    private fun startTicking(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val diffMs = endTimestamp - now
                val remSec = (diffMs / 1000L).coerceAtLeast(0).toInt()

                if (remSec <= 0) {
                    _uiState.value = _uiState.value.copy(remainingSeconds = 0)
                    completeSession(isSkip = false, soundEnabled = soundEnabled, vibrationEnabled = vibrationEnabled)
                    break
                } else {
                    _uiState.value = _uiState.value.copy(remainingSeconds = remSec)
                }
                delay(500)
            }
        }
    }

    private fun completeSession(isSkip: Boolean, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        val current = _uiState.value
        val wasFocus = current.mode == SessionMode.FOCUS

        // Feedback
        hapticHelper.playCompletionFeedback(soundEnabled, vibrationEnabled)

        // Notification
        val title = if (wasFocus) "Focus Session Finished!" else "Break Finished!"
        val msg = if (wasFocus) {
            "Great work on ${current.attachedTask?.title ?: "your focus session"}. Ready for a mindful break?"
        } else {
            "Feeling refreshed? Ready to resume your focus."
        }
        notificationHelper.showSessionCompletedNotification(title, msg)

        // Save session record to DB
        scope.launch {
            val completedSeconds = if (isSkip) {
                current.totalSeconds - current.remainingSeconds
            } else {
                current.totalSeconds
            }
            repository.recordSession(
                sessionType = current.mode,
                durationMinutes = current.totalSeconds / 60,
                completedSeconds = completedSeconds,
                taskId = current.attachedTask?.id,
                taskTitle = current.attachedTask?.title
            )
        }

        val nextSessionNum = if (wasFocus) current.sessionNumber + 1 else current.sessionNumber
        val nextMode = if (wasFocus) {
            if (nextSessionNum % 4 == 0) SessionMode.LONG_BREAK else SessionMode.SHORT_BREAK
        } else {
            SessionMode.FOCUS
        }

        _uiState.value = current.copy(
            state = TimerState.COMPLETED,
            sessionNumber = nextSessionNum,
            autoBreakSuggested = wasFocus
        )
    }

    fun dismissBreakSuggestion() {
        _uiState.value = _uiState.value.copy(autoBreakSuggested = false)
    }
}
