package com.pixelfitquest.firebase.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.local.CloudSyncPolicy
import com.pixelfitquest.local.LocalPixelFitStore
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
        auth: FirebaseAuth,
        localStore: LocalPixelFitStore,
        cloudSyncPolicy: CloudSyncPolicy,
    ): UserRepository = UserRepository(firestore, auth, localStore, cloudSyncPolicy)
}