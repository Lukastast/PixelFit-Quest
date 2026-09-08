package com.pixelfitquest.firebase.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.pixelfitquest.local.CloudSyncPolicy
import com.pixelfitquest.local.StubCloudSyncPolicy
import com.pixelfitquest.local.db.PixelFitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .build()
            )
            .build()
        db.firestoreSettings = settings
        return db
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("pixelfitquest_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideCloudSyncPolicy(): CloudSyncPolicy = StubCloudSyncPolicy()

    @Provides
    @Singleton
    fun providePixelFitDatabase(@ApplicationContext context: Context): PixelFitDatabase {
        return Room.databaseBuilder(
            context,
            PixelFitDatabase::class.java,
            "pixelfit.db",
        ).build()
    }
}