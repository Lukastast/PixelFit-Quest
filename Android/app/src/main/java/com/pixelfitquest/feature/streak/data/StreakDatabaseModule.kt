package com.pixelfitquest.feature.streak.data

import android.content.Context
import androidx.room.Room
import com.pixelfitquest.feature.streak.model.StreakClock
import com.pixelfitquest.feature.streak.model.SystemStreakClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StreakDatabaseModule {
    @Provides
    @Singleton
    fun provideStreakDatabase(@ApplicationContext context: Context): StreakDatabase {
        return Room.databaseBuilder(
            context,
            StreakDatabase::class.java,
            "weekly_streak.db",
        ).build()
    }

    @Provides
    fun provideWeeklyStreakDao(database: StreakDatabase): WeeklyStreakDao =
        database.weeklyStreakDao()

    @Provides
    @Singleton
    fun provideStreakClock(): StreakClock = SystemStreakClock()
}
