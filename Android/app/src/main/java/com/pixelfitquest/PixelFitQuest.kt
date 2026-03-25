package com.pixelfitquest

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PixelFitQuest : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}