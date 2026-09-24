package com.pixelfitquest.feature.achievements

import com.pixelfitquest.feature.achievements.model.AchievementCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCatalogTest {

    @Test
    fun idsAreUnique() {
        val ids = AchievementCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun expandsBeyondOriginalThree() {
        assertTrue(AchievementCatalog.all.size > 3)
        assertTrue(
            AchievementCatalog.all.map { it.id }.containsAll(AchievementCatalog.legacyWorkoutIds),
        )
    }

    @Test
    fun everyEntryHasCoinsOrXpHook() {
        AchievementCatalog.all.forEach { definition ->
            assertTrue(
                "${definition.id} needs a coins/XP hook",
                definition.reward.hasValue,
            )
        }
    }

    @Test
    fun coversMultipleCategoriesAndMetrics() {
        assertTrue(AchievementCatalog.all.map { it.category }.toSet().size >= 4)
        assertTrue(AchievementCatalog.all.map { it.metric }.toSet().size >= 4)
    }

    @Test
    fun everyCatalogEntryHasDedicatedDrawableIcon() {
        val iconResIds = AchievementCatalog.all.map { def ->
            achievementIconRes(def.id, def.tier)
        }
        iconResIds.forEach { resId ->
            assertTrue(resId != 0)
        }
        assertEquals(
            "Every catalog achievement should have a distinct dedicated icon",
            AchievementCatalog.all.size,
            iconResIds.toSet().size,
        )
    }
}
