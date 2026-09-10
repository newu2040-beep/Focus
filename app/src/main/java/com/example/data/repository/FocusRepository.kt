package com.example.data.repository

import com.example.data.dao.DailyReviewDao
import com.example.data.dao.FocusSessionDao
import com.example.data.dao.NoteDao
import com.example.data.dao.TaskDao
import com.example.data.model.DailyReviewEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.NoteEntity
import com.example.data.model.Priority
import com.example.data.model.SessionMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusRepository(
    private val taskDao: TaskDao,
    private val sessionDao: FocusSessionDao,
    private val noteDao: NoteDao,
    private val dailyReviewDao: DailyReviewDao
) {
    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    val todayPriority: Flow<TaskEntity?> = taskDao.getTodayPriority()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)
    suspend fun clearCompletedTasks() = taskDao.clearCompletedTasks()

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
    }

    suspend fun setTodayPriority(task: TaskEntity, isPriority: Boolean) {
        val updated = task.copy(isTodayPriority = isPriority)
        taskDao.updateTask(updated)
    }

    // Sessions
    val allSessions: Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsToday(): Flow<List<FocusSessionEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return sessionDao.getSessionsSince(calendar.timeInMillis)
    }

    fun getSessionsThisWeek(): Flow<List<FocusSessionEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return sessionDao.getSessionsSince(calendar.timeInMillis)
    }

    suspend fun recordSession(
        sessionType: SessionMode,
        durationMinutes: Int,
        completedSeconds: Int,
        taskId: Long? = null,
        taskTitle: String? = null
    ): Long {
        val session = FocusSessionEntity(
            taskId = taskId,
            taskTitle = taskTitle,
            sessionType = sessionType,
            durationMinutes = durationMinutes,
            completedSeconds = completedSeconds,
            isCompleted = completedSeconds >= (durationMinutes * 60 - 5),
            timestamp = System.currentTimeMillis()
        )
        return sessionDao.insertSession(session)
    }

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        return if (query.isBlank()) noteDao.getAllNotes() else noteDao.searchNotes(query.trim())
    }

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    // Daily Review
    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getReviewForToday(): Flow<DailyReviewEntity?> {
        return dailyReviewDao.getReviewForDate(getTodayDateKey())
    }

    suspend fun saveDailyReview(review: DailyReviewEntity) {
        dailyReviewDao.insertReview(review)
    }

    // Clear all data
    suspend fun clearAllData() {
        taskDao.clearAllTasks()
        sessionDao.clearAllSessions()
        noteDao.clearAllNotes()
        dailyReviewDao.clearAllReviews()
    }

    // Export Data to JSON
    suspend fun exportDataJson(tasks: List<TaskEntity>, sessions: List<FocusSessionEntity>, notes: List<NoteEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val tasksArray = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject()
            obj.put("title", task.title)
            obj.put("notes", task.notes)
            obj.put("priority", task.priority.name)
            obj.put("category", task.category.name)
            obj.put("dueDate", task.dueDate ?: 0L)
            obj.put("dueTime", task.dueTime ?: "")
            obj.put("estimatedMinutes", task.estimatedMinutes)
            obj.put("isCompleted", task.isCompleted)
            obj.put("isTodayPriority", task.isTodayPriority)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val notesArray = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject()
            obj.put("title", note.title)
            obj.put("content", note.content)
            obj.put("isPinned", note.isPinned)
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        val sessionsArray = JSONArray()
        sessions.forEach { s ->
            val obj = JSONObject()
            obj.put("taskTitle", s.taskTitle ?: "")
            obj.put("sessionType", s.sessionType.name)
            obj.put("durationMinutes", s.durationMinutes)
            obj.put("completedSeconds", s.completedSeconds)
            obj.put("timestamp", s.timestamp)
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)

        return root.toString(2)
    }

    // Import Data from JSON (validates and adds items safely)
    suspend fun importDataJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val priority = try { Priority.valueOf(obj.optString("priority", "MEDIUM")) } catch (_: Exception) { Priority.MEDIUM }
                    val category = try { TaskCategory.valueOf(obj.optString("category", "WORK")) } catch (_: Exception) { TaskCategory.WORK }
                    val task = TaskEntity(
                        title = obj.optString("title", "Imported task"),
                        notes = obj.optString("notes", ""),
                        priority = priority,
                        category = category,
                        dueDate = if (obj.optLong("dueDate", 0L) > 0) obj.getLong("dueDate") else null,
                        dueTime = obj.optString("dueTime", null),
                        estimatedMinutes = obj.optInt("estimatedMinutes", 25),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        isTodayPriority = obj.optBoolean("isTodayPriority", false)
                    )
                    taskDao.insertTask(task)
                }
            }

            if (root.has("notes")) {
                val notesArray = root.getJSONArray("notes")
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    val note = NoteEntity(
                        title = obj.optString("title", "Imported note"),
                        content = obj.optString("content", ""),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                    noteDao.insertNote(note)
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}
