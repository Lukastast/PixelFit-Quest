package com.pixelfitquest.feature.achievements.di

import com.pixelfitquest.feature.achievements.data.AchievementProgressStore
import com.pixelfitquest.feature.achievements.data.AchievementsRepository
import com.pixelfitquest.feature.achievements.data.DefaultAchievementsRepository
import com.pixelfitquest.feature.achievements.data.RoomAchievementProgressStore
import com.pixelfitquest.feature.achievements.rewards.AchievementRewardSink
import com.pixelfitquest.feature.achievements.rewards.DeferredCoinsXpRewardSink
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AchievementsBindModule {
    @Binds
    @Singleton
    abstract fun bindProgressStore(impl: RoomAchievementProgressStore): AchievementProgressStore

    @Binds
    @Singleton
    abstract fun bindRepository(impl: DefaultAchievementsRepository): AchievementsRepository

    @Binds
    @Singleton
    abstract fun bindRewardSink(impl: DeferredCoinsXpRewardSink): AchievementRewardSink
}
