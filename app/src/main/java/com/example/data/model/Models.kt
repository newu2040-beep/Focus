package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class TaskCategory(val displayName: String) {
    WORK("Work"),
    PERSONAL("Personal"),
    HEALTH("Health"),
    STUDY("Study"),
    HOBBY("Hobby"),
    OTHER("Other")
}

enum class SessionMode(val displayName: String, val defaultMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15),
    CUSTOM("Custom", 30)
}

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AccentTheme(val displayName: String) {
    SAGE("Calm Sage"),
    OCEAN("Ocean Mist"),
    AMBER("Warm Amber"),
    ROSE("Rose Clay")
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val estimatedMinutes: Int = 25,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isTodayPriority: Boolean = false,
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val sessionType: SessionMode = SessionMode.FOCUS,
    val durationMinutes: Int,
    val completedSeconds: Int,
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val taskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_reviews")
data class DailyReviewEntity(
    @PrimaryKey val dateKey: String, // format: YYYY-MM-DD
    val rating: Int = 4,
    val whatWentWell: String = "",
    val distractions: String = "",
    val tomorrowPriority: String = "",
    val completedTasksCount: Int = 0,
    val focusMinutes: Int = 0,
    val completedSessionsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
