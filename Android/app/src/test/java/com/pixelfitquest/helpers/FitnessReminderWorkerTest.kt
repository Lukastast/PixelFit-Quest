package com.pixelfitquest.helpers

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.pixelfitquest.repository.WorkoutRepository
import com.pixelfitquest.model.Workout
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import kotlin.test.assertEquals

class FitnessReminderWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        workoutRepository = mockk(relaxed = true)

        mockkObject(NotificationHelper)
        // Mock the notification method to do nothing (just run successfully)
        every { NotificationHelper.showWorkoutReminderNotification(any()) } just runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `doWork shows reminder when needed`() = runTest {
        // Create a workout with an old date (not today)
        val oldWorkout = Workout(
            id = "workout1",
            date = "2023-01-01",  // Old date
            name = "Test Workout",
            totalExercises = 1,
            totalSets = 3,
            overallScore = 85f
        )

        coEvery { workoutRepository.fetchWorkoutsOnce(1) } returns listOf(oldWorkout)

        val worker = FitnessReminderWorker(context, workerParams, workoutRepository)
        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 1) { NotificationHelper.showWorkoutReminderNotification(context) }
    }

    @Test
    fun `doWork does not show reminder when not needed`() = runTest {
        // Dynamically get today's date in YYYY-MM-DD format
        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

        // Create a workout with today's date
        val todayWorkout = Workout(
            id = "workout2",
            date = today,
            name = "Today's Workout",
            totalExercises = 2,
            totalSets = 5,
            overallScore = 90f
        )

        coEvery { workoutRepository.fetchWorkoutsOnce(1) } returns listOf(todayWorkout)

        val worker = FitnessReminderWorker(context, workerParams, workoutRepository)
        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 0) { NotificationHelper.showWorkoutReminderNotification(any()) }
    }
    @Test
    fun `doWork shows reminder when no workouts exist`() = runTest {
        coEvery { workoutRepository.fetchWorkoutsOnce(1) } returns emptyList()

        val worker = FitnessReminderWorker(context, workerParams, workoutRepository)
        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 1) { NotificationHelper.showWorkoutReminderNotification(context) }
    }

    @Test
    fun `doWork retries on exception`() = runTest {
        coEvery { workoutRepository.fetchWorkoutsOnce(1) } throws RuntimeException("Test failure")

        val worker = FitnessReminderWorker(context, workerParams, workoutRepository)
        val result = worker.doWork()

        assertEquals(Result.retry(), result)
        verify(exactly = 0) { NotificationHelper.showWorkoutReminderNotification(any()) }
    }
}