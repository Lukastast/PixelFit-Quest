package com.pixelfitquest.feature.levels.data

import com.pixelfitquest.feature.levels.cosmetics.NoOpCloudProgressMirror
import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelsRepositoryTest {

    private fun repo(): DefaultLevelsRepository = DefaultLevelsRepository(
        store = InMemoryLevelsStore(),
        cloudMirror = NoOpCloudProgressMirror(),
    )

    @Test
    fun awardXp_levelsUpAndUnlocksGymTheme() = runBlocking {
        val repository = repo()
        val result = repository.awardXp(100 + 200, "test")
        assertTrue(result.leveledUp)
        assertEquals(3, result.current.level)
        assertTrue(result.newlyUnlocked.any { it.id == CosmeticCatalog.HOME_GYM })

        val snapshot = repository.observeSnapshot().first()
        assertEquals(3, snapshot.progress.level)
        assertTrue(snapshot.items.first { it.definition.id == CosmeticCatalog.HOME_GYM }.unlocked)
    }

    @Test
    fun equipCosmetic_rejectsLocked() = runBlocking {
        val repository = repo()
        assertFalse(repository.equipCosmetic(CosmeticCatalog.HOME_LEGEND))
        repository.awardXp(LevelCurveTotalFor(25), "grind")
        assertTrue(repository.equipCosmetic(CosmeticCatalog.HOME_LEGEND))
        val snapshot = repository.observeSnapshot().first()
        assertEquals(CosmeticCatalog.HOME_LEGEND, snapshot.equipped.homeThemeId)
    }

    @Test
    fun importRemoteIfEmpty_seedsOnce() = runBlocking {
        val repository = repo()
        repository.importRemoteIfEmpty(remoteLevel = 5, remoteXpIntoLevel = 10)
        val first = repository.observeProgress().first()
        assertEquals(5, first.level)
        assertEquals(10, first.xpIntoLevel)

        repository.importRemoteIfEmpty(remoteLevel = 20, remoteXpIntoLevel = 0)
        val second = repository.observeProgress().first()
        assertEquals(5, second.level)
        assertEquals(10, second.xpIntoLevel)
    }

    @Test
    fun awardXp_zeroIsNoOp() = runBlocking {
        val repository = repo()
        val result = repository.awardXp(0, "noop")
        assertFalse(result.leveledUp)
        assertEquals(1, result.current.level)
        assertEquals(0, result.current.totalXp)
    }

    @Test
    fun cloudMirror_isNeverRequired() = runBlocking {
        var mirrored = false
        val repository = DefaultLevelsRepository(
            store = InMemoryLevelsStore(),
            cloudMirror = object : com.pixelfitquest.feature.levels.cosmetics.CloudProgressMirror {
                override suspend fun mirror(progress: com.pixelfitquest.feature.levels.model.LevelProgress) {
                    mirrored = true
                }
            },
        )
        repository.awardXp(50, "mirror")
        assertTrue(mirrored)
    }
}

private fun LevelCurveTotalFor(level: Int): Int {
    return com.pixelfitquest.feature.levels.progression.LevelCurve.totalXpForLevel(level)
}
