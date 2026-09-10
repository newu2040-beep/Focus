package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.Priority
import com.example.data.model.SessionMode
import com.example.data.model.TaskCategory

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (_: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = try {
        TaskCategory.valueOf(value)
    } catch (_: Exception) {
        TaskCategory.OTHER
    }

    @TypeConverter
    fun fromSessionMode(mode: SessionMode): String = mode.name

    @TypeConverter
    fun toSessionMode(value: String): SessionMode = try {
        SessionMode.valueOf(value)
    } catch (_: Exception) {
        SessionMode.FOCUS
    }
}
