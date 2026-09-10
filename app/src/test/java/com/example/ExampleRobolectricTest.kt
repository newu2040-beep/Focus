package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Priority
import com.example.data.model.SessionMode
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.timer.TimerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Focus Guide", appName)
    }

    @Test
    fun `timer formatted time calculation`() {
        val timerState = TimerUiState(
            totalSeconds = 25 * 60,
            remainingSeconds = 25 * 60
        )
        assertEquals("25:00", timerState.formattedTime)
        assertEquals(0f, timerState.progress)

        val halfwayTimer = timerState.copy(remainingSeconds = 12 * 60 + 30)
        assertEquals("12:30", halfwayTimer.formattedTime)
        assertTrue(halfwayTimer.progress in 0.49f..0.51f)
    }

    @Test
    fun `task entity default values and creation`() {
        val task = TaskEntity(
            title = "Deep Focus Task",
            priority = Priority.HIGH,
            category = TaskCategory.WORK
        )
        assertEquals("Deep Focus Task", task.title)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(TaskCategory.WORK, task.category)
        assertEquals(false, task.isCompleted)
    }

    @Test
    fun `session mode default minutes`() {
        assertEquals(25, SessionMode.FOCUS.defaultMinutes)
        assertEquals(5, SessionMode.SHORT_BREAK.defaultMinutes)
        assertEquals(15, SessionMode.LONG_BREAK.defaultMinutes)
    }
}
