package com.pixelfitquest.feature.progress.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProgressDatabaseModule {
    @Provides
    @Singleton
    fun provideProgressDatabase(@ApplicationContext context: Context): ProgressDatabase {
        return Room.databaseBuilder(
            context,
            ProgressDatabase::class.java,
            "progress_history.db",
        ).build()
    }

    @Provides
    fun provideLiftHistoryDao(database: ProgressDatabase): LiftHistoryDao = database.liftHistoryDao()
}
