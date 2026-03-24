package com.pixelfitquest

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.pixelfitquest.helpers.FitnessReminderWorker
import com.pixelfitquest.helpers.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PixelFitQuest : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Firebase globally
        FirebaseApp.initializeApp(this)

        // 2. Create Notification Channels once
        NotificationHelper.createNotificationChannel(this)

        // 3. Schedule your daily workout reminder
        val dailyWorkRequest = PeriodicWorkRequestBuilder<FitnessReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "workout_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}