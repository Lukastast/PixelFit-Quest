package com.pixelfitquest.helpers

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var mockCompat: NotificationManagerCompat

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        mockkStatic(NotificationManagerCompat::class)
        mockCompat = mockk(relaxed = true)
        every { NotificationManagerCompat.from(any()) } returns mockCompat

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createNotificationChannel creates channel on O and above`() {
        NotificationHelper.createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel("pixel_fit_quest_channel")

        assertNotNull(channel)
        assertEquals("Pixel Fit Quest Notifications", channel.name.toString())
        assertEquals("Notifications for fitness goals and reminders", channel.description)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
    }

    @Test
    fun `showStepGoalCompletedNotification sends notification`() {
        NotificationHelper.showStepGoalCompletedNotification(context)

        verify(exactly = 1) { mockCompat.notify(eq(1), any()) }
    }

    @Test
    fun `showStepGoalCompletedNotification handles exception without crashing`() {
        every { mockCompat.notify(any(), any()) } throws SecurityException("Missing permission")

        // Should not crash
        NotificationHelper.showStepGoalCompletedNotification(context)

        verify { Log.e("NotificationHelper", "Failed to show step goal notification: missing permission", any()) }
    }

    @Test
    fun `showWorkoutCompletedNotification sends notification`() {
        NotificationHelper.showWorkoutCompletedNotification(context)

        verify(exactly = 1) { mockCompat.notify(eq(2), any()) }
    }

    @Test
    fun `showWorkoutCompletedNotification handles exception without crashing`() {
        every { mockCompat.notify(any(), any()) } throws SecurityException("Missing permission")

        NotificationHelper.showWorkoutCompletedNotification(context)

        verify { Log.e("NotificationHelper", "Failed to show workout completed notification: missing permission", any()) }
    }

    @Test
    fun `showWorkoutReminderNotification sends notification`() {
        NotificationHelper.showWorkoutReminderNotification(context)

        verify(exactly = 1) { mockCompat.notify(eq(3), any()) }
    }

    @Test
    fun `showWorkoutReminderNotification handles exception without crashing`() {
        every { mockCompat.notify(any(), any()) } throws SecurityException("Missing permission")

        NotificationHelper.showWorkoutReminderNotification(context)

        verify { Log.e("NotificationHelper", "Failed to show workout reminder notification: missing permission", any()) }
    }

    @Test
    fun `showStepGoalReminderNotification sends notification`() {
        NotificationHelper.showStepGoalReminderNotification(context)

        verify(exactly = 1) { mockCompat.notify(eq(4), any()) }
    }

    @Test
    fun `showStepGoalReminderNotification handles exception without crashing`() {
        every { mockCompat.notify(any(), any()) } throws SecurityException("Missing permission")

        NotificationHelper.showStepGoalReminderNotification(context)

        verify { Log.e("NotificationHelper", "Failed to show step goal reminder notification: missing permission", any()) }
    }

    @Test
    fun `showMissionCompletedNotification sends notification`() {
        NotificationHelper.showMissionCompletedNotification(context, "Daily Run", "50 coins")

        verify(exactly = 1) { mockCompat.notify(eq(5), any()) }
    }

    @Test
    fun `showMissionCompletedNotification handles exception without crashing`() {
        every { mockCompat.notify(any(), any()) } throws SecurityException("Missing permission")

        NotificationHelper.showMissionCompletedNotification(context, "Daily Run", "50 coins")

        verify { Log.e("NotificationHelper", "Failed to show mission completed notification: missing permission", any()) }
    }
}