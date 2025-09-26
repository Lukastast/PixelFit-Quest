package com.PixelFitQuest.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.LocalCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder(db.firestoreSettings)
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
}