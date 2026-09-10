package com.example.data.dao

import androidx.room.*
import com.example.data.model.DailyReviewEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.NoteEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, isTodayPriority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isTodayPriority = 1 AND isCompleted = 0 LIMIT 1")
    fun getTodayPriority(): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY isTodayPriority DESC, createdAt DESC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSessionsSince(startTime: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getSessionsBetween(startTime: Long, endTime: Long): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()
}

@Dao
interface DailyReviewDao {
    @Query("SELECT * FROM daily_reviews WHERE dateKey = :dateKey LIMIT 1")
    fun getReviewForDate(dateKey: String): Flow<DailyReviewEntity?>

    @Query("SELECT * FROM daily_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<DailyReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: DailyReviewEntity)

    @Query("DELETE FROM daily_reviews")
    suspend fun clearAllReviews()
}
