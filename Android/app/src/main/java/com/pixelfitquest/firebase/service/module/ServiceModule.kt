package com.pixelfitquest.firebase.service.module

import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.firebase.service.impl.AccountServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds abstract fun provideAccountService(impl: AccountServiceImpl): AccountService
}