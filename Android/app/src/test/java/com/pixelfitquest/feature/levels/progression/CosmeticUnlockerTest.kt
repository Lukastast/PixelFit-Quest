package com.pixelfitquest.feature.levels.progression

import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import com.pixelfitquest.feature.levels.model.CosmeticKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CosmeticUnlockerTest {

    @Test
    fun levelOne_unlocksDefaultsOnly() {
        val ids = CosmeticUnlocker.unlockedIds(1)
        assertTrue(CosmeticCatalog.HOME_STONE in ids)
        assertTrue(CosmeticCatalog.SKIN_BASIC in ids)
        assertTrue(CosmeticCatalog.TITLE_ROOKIE in ids)
        assertFalse(CosmeticCatalog.HOME_GYM in ids)
        assertFalse(CosmeticCatalog.SKIN_FITNESS in ids)
    }

    @Test
    fun gymThemeUnlocksAtThree() {
        assertFalse(CosmeticCatalog.HOME_GYM in CosmeticUnlocker.unlockedIds(2))
        assertTrue(CosmeticCatalog.HOME_GYM in CosmeticUnlocker.unlockedIds(3))
    }

    @Test
    fun newlyUnlocked_returnsOnlyCrossedThresholds() {
        val newly = CosmeticUnlocker.newlyUnlocked(previousLevel = 4, newLevel = 5)
        assertEquals(1, newly.size)
        assertEquals(CosmeticCatalog.SKIN_FITNESS, newly.first().id)
        assertEquals(CosmeticKind.CHARACTER_SKIN, newly.first().kind)
    }

    @Test
    fun newlyUnlocked_emptyWhenNoGain() {
        assertTrue(CosmeticUnlocker.newlyUnlocked(10, 10).isEmpty())
        assertTrue(CosmeticUnlocker.newlyUnlocked(12, 8).isEmpty())
    }

    @Test
    fun catalog_coversHomeSkinsAndTitles() {
        val byKind = CosmeticCatalog.all.groupBy { it.kind }
        assertTrue(byKind[CosmeticKind.HOME_THEME]!!.size >= 5)
        assertTrue(byKind[CosmeticKind.CHARACTER_SKIN]!!.size >= 3)
        assertTrue(byKind[CosmeticKind.TITLE]!!.size >= 4)
        assertEquals(CosmeticCatalog.all.size, CosmeticCatalog.all.map { it.id }.toSet().size)
    }
}
