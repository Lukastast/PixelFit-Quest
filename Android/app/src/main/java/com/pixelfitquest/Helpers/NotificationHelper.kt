package com.pixelfitquest.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pixelfitquest.R

object NotificationHelper {
    private const val CHANNEL_ID = "pixel_fit_quest_channel"
    private const val CHANNEL_NAME = "Pixel Fit Quest Notifications"
    private const val CHANNEL_DESC = "Notifications for fitness goals and reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showStepGoalCompletedNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pixelfiticon)  // Use default icon
            .setContentTitle("Step Goal Achieved!")
            .setContentText("Congratulations! You've reached your daily step goal. Keep it up!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(1, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle permission denial
        }
    }

    fun showWorkoutCompletedNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pixelfiticon)
            .setContentTitle("Workout Completed!")
            .setContentText("Great job on finishing your workout. You're getting stronger!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(2, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle permission denial
        }
    }

    fun showWorkoutReminderNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pixelfiticon)
            .setContentTitle("Workout Reminder")
            .setContentText("You haven't worked out today. Time to get moving and maintain your streak!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(3, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle permission denial
        }
    }

    fun showStepGoalReminderNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pixelfiticon)
            .setContentTitle("Step Goal Reminder")
            .setContentText("You're making progress—keep going to hit your daily step goal!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(4, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle permission denial
        }
    }

    fun showMissionCompletedNotification(context: Context, mission: String, reward: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pixelfiticon)
            .setContentTitle("Mission Completed!")
            .setContentText("You completed: $mission. Reward: +$reward")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(5, builder.build())  // Unique ID for mission notifications
            }
        } catch (e: SecurityException) {
            // Handle permission denial
        }
    }
}