package com.pixelfitquest.feature.levels.di

import com.pixelfitquest.feature.levels.cosmetics.CharacterSkinPort
import com.pixelfitquest.feature.levels.cosmetics.CloudProgressMirror
import com.pixelfitquest.feature.levels.cosmetics.HomeThemePort
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.feature.levels.cosmetics.NoOpCloudProgressMirror
import com.pixelfitquest.feature.levels.data.DefaultLevelsRepository
import com.pixelfitquest.feature.levels.data.LevelsRepository
import com.pixelfitquest.feature.levels.data.LevelsStore
import com.pixelfitquest.feature.levels.data.LocalProfileXpSource
import com.pixelfitquest.feature.levels.data.ProfileXpSource
import com.pixelfitquest.feature.levels.data.RoomLevelsStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LevelsModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: DefaultLevelsRepository): LevelsRepository

    @Binds
    @Singleton
    abstract fun bindStore(impl: RoomLevelsStore): LevelsStore

    @Binds
    @Singleton
    abstract fun bindXpSource(impl: LocalProfileXpSource): ProfileXpSource

    companion object {
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
