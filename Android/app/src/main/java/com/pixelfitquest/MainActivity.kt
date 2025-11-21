package com.pixelfitquest

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.google.firebase.FirebaseApp
import com.pixelfitquest.ui.navigation.AppScaffold
import com.pixelfitquest.utils.FitnessReminderWorker
import com.pixelfitquest.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            // Optional: Handle denial (e.g., show explanation)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // Lock to portrait (vertical) orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule daily workout reminder worker
        val dailyWorkRequest = PeriodicWorkRequestBuilder<FitnessReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "workout_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        setContent {
            PixelFitQuestTheme {
                AppScaffold()
            }
        }
    }
}