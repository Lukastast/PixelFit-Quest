package com.pixelfitquest.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixelfitquest.repository.WorkoutRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@HiltWorker
class FitnessReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val workoutRepository: WorkoutRepository  // Injected via Hilt
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Check for workout reminder (using existing repo method)
            val latestWorkouts = workoutRepository.fetchWorkoutsOnce(1)
            val lastWorkoutDateStr = latestWorkouts.firstOrNull()?.date ?: ""
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val needsWorkoutReminder = lastWorkoutDateStr != today

            if (needsWorkoutReminder) {
                NotificationHelper.showWorkoutReminderNotification(applicationContext)
                NotificationHelper.showWorkoutReminderNotification(applicationContext)
            }

            // Future: Add other checks here (e.g., if you add a way to check steps or other goals in background)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()  // Retry on failure
        }
    }
}