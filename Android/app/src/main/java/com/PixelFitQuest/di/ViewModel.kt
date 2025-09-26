package com.PixelFitQuest.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.PixelFitQuest.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @Provides
    @ViewModelScoped
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository = UserRepository(firestore, auth)
}