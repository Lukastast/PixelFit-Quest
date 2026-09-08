package com.pixelfitquest.feature.levels.di

import android.content.Context
import androidx.room.Room
import com.pixelfitquest.feature.levels.cosmetics.CharacterSkinPort
import com.pixelfitquest.feature.levels.cosmetics.CloudProgressMirror
import com.pixelfitquest.feature.levels.cosmetics.HomeThemePort
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.feature.levels.cosmetics.NoOpCloudProgressMirror
import com.pixelfitquest.feature.levels.data.DefaultLevelsRepository
import com.pixelfitquest.feature.levels.data.LevelsDao
import com.pixelfitquest.feature.levels.data.LevelsDatabase
import com.pixelfitquest.feature.levels.data.LevelsRepository
import com.pixelfitquest.feature.levels.data.LevelsStore
import com.pixelfitquest.feature.levels.data.RoomLevelsStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LevelsModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: DefaultLevelsRepository): LevelsRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): LevelsDatabase {
            return Room.databaseBuilder(
                context,
                LevelsDatabase::class.java,
                "levels.db",
            ).fallbackToDestructiveMigration(dropAllTables = true).build()
        }

        @Provides
        fun provideDao(db: LevelsDatabase): LevelsDao = db.levelsDao()

        @Provides
        @Singleton
        fun provideStore(dao: LevelsDao): LevelsStore = RoomLevelsStore(dao)

        @Provides
        @Singleton
        fun provideCloudMirror(): CloudProgressMirror = NoOpCloudProgressMirror()

        @Provides
        fun provideLocalXp(repo: LevelsRepository): LocalXpPort = repo

        @Provides
        fun provideHomeTheme(repo: LevelsRepository): HomeThemePort = repo

        @Provides
        fun provideCharacterSkin(repo: LevelsRepository): CharacterSkinPort = repo
    }
}
