package com.pixelfitquest.feature.achievements.di

import android.content.Context
import androidx.room.Room
import com.pixelfitquest.feature.achievements.data.AchievementDao
import com.pixelfitquest.feature.achievements.data.AchievementProgressStore
import com.pixelfitquest.feature.achievements.data.AchievementsDatabase
import com.pixelfitquest.feature.achievements.data.AchievementsRepository
import com.pixelfitquest.feature.achievements.data.DefaultAchievementsRepository
import com.pixelfitquest.feature.achievements.data.RoomAchievementProgressStore
import com.pixelfitquest.feature.achievements.rewards.AchievementRewardSink
import com.pixelfitquest.feature.achievements.rewards.DeferredCoinsXpRewardSink
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

@Module
@InstallIn(SingletonComponent::class)
object AchievementsProvideModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AchievementsDatabase {
        return Room.databaseBuilder(
            context,
            AchievementsDatabase::class.java,
            "achievements.db",
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideDao(db: AchievementsDatabase): AchievementDao = db.achievementDao()
}
