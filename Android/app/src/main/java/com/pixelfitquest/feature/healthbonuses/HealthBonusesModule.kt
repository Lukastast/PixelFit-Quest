package com.pixelfitquest.feature.healthbonuses

import android.content.Context
import com.pixelfitquest.health.HealthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthBonusesModule {

    @Provides
    @Singleton
    fun provideSessionBonusStore(
        @ApplicationContext context: Context,
    ): SessionBonusStore = PrefsSessionBonusStore(context)

    @Provides
    @Singleton
    fun provideSessionBonusResolver(
        store: SessionBonusStore,
    ): SessionBonusResolver = SessionBonusResolver(store) {
        LocalDate.now(ZoneId.systemDefault()).toString()
    }

    /** Health metrics for session bonuses come from Health Connect only. */
    @Provides
    @Singleton
    fun provideHealthMetricsSource(
        healthRepository: HealthRepository,
    ): HealthMetricsSource = HealthConnectMetricsSource(healthRepository)
}
