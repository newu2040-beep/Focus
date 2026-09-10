package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        FocusSessionEntity::class,
        NoteEntity::class,
        DailyReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun noteDao(): NoteDao
    abstract fun dailyReviewDao(): DailyReviewDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_guide_database"
                )
                .addCallback(FocusDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FocusDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: FocusDatabase) {
            val taskDao = db.taskDao()
            val noteDao = db.noteDao()
            val sessionDao = db.focusSessionDao()

            val now = System.currentTimeMillis()

            // Initial starter tasks reflecting clean productivity habits
            taskDao.insertTask(
                TaskEntity(
                    title = "Review today's top priorities",
                    notes = "Spend 5 minutes planning the core objective for the day.",
                    priority = Priority.HIGH,
                    category = TaskCategory.WORK,
                    isTodayPriority = true,
                    estimatedMinutes = 15,
                    dueTime = "9:00 AM",
                    createdAt = now
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Deep work on main project",
                    notes = "Turn off notifications and focus for 25 uninterrupted minutes.",
                    priority = Priority.HIGH,
                    category = TaskCategory.WORK,
                    isTodayPriority = false,
                    estimatedMinutes = 45,
                    dueTime = "11:00 AM",
                    createdAt = now + 1
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Afternoon walk & reset",
                    notes = "Step outside for fresh air and gentle movement.",
                    priority = Priority.MEDIUM,
                    category = TaskCategory.HEALTH,
                    isTodayPriority = false,
                    estimatedMinutes = 20,
                    dueTime = "3:30 PM",
                    createdAt = now + 2
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Read 10 pages of a book",
                    notes = "Quiet evening reading habit.",
                    priority = Priority.LOW,
                    category = TaskCategory.HOBBY,
                    isTodayPriority = false,
                    estimatedMinutes = 20,
                    dueTime = "8:00 PM",
                    createdAt = now + 3
                )
            )

            // Starter Notes
            noteDao.insertNote(
                NoteEntity(
                    title = "Ideas for the future",
                    content = "Build something meaningful that brings calm and clarity to everyday life.",
                    isPinned = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
            noteDao.insertNote(
                NoteEntity(
                    title = "Mindful Focus Rule",
                    content = "Do one thing at a time with full presence. Quality over speed.",
                    isPinned = false,
                    createdAt = now - 86400000L,
                    updatedAt = now - 86400000L
                )
            )

            // A completed session to show baseline progress
            sessionDao.insertSession(
                FocusSessionEntity(
                    taskTitle = "Morning planning",
                    sessionType = SessionMode.FOCUS,
                    durationMinutes = 25,
                    completedSeconds = 1500,
                    isCompleted = true,
                    timestamp = now - 3600000L
                )
            )
        }
    }
}
