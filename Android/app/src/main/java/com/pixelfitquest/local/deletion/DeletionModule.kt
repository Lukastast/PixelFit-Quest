package com.pixelfitquest.local.deletion

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeletionModule {
    @Binds
    @Singleton
    abstract fun bindLocalDataWipe(impl: AndroidLocalDataWipe): LocalDataWipe
}
