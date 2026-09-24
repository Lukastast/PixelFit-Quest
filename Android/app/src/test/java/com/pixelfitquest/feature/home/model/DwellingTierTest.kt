package com.pixelfitquest.feature.home.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DwellingTierTest {

    @Test
    fun legacyGrant_matchesHomesTheOldCurveGaveAway() {
        assertEquals(setOf(DwellingTier.TARP.id), DwellingTier.legacyGrantedIds(1))
        assertTrue(DwellingTier.TENT.id in DwellingTier.legacyGrantedIds(5))
        assertFalse(DwellingTier.SHACK.id in DwellingTier.legacyGrantedIds(9))
        assertTrue(DwellingTier.CASTLE.id in DwellingTier.legacyGrantedIds(25))
        assertFalse(DwellingTier.GYM.id in DwellingTier.legacyGrantedIds(100))
    }

    @Test
    fun migrateOwnership_grantsOnceAndKeepsPurchases() {
        val first = DwellingTier.migrateOwnership(
            level = 25,
            ownedIds = listOf(DwellingTier.GYM.id, "basic"),
            alreadyMigrated = false,
        )
        assertTrue(DwellingTier.CASTLE.id in first)
        assertTrue(DwellingTier.GYM.id in first)
        assertFalse("basic" in first)

        val later = DwellingTier.migrateOwnership(
            level = 70,
            ownedIds = first,
            alreadyMigrated = true,
        )
        assertEquals(first, later)
    }

    @Test
    fun resolve_usesWhatYouOwn() {
        assertEquals(DwellingTier.TARP, DwellingTier.resolve(null, emptyList()))
        assertEquals(
            DwellingTier.CASTLE,
            DwellingTier.resolve(null, listOf(DwellingTier.TARP.id, DwellingTier.CASTLE.id)),
        )
        assertEquals(
            DwellingTier.TENT,
            DwellingTier.resolve(DwellingTier.TENT.id, listOf(DwellingTier.CASTLE.id)),
        )
    }

    @Test
    fun purchaseGates_areSpreadPastTheOldFreeLevels() {
        assertEquals(5, DwellingTier.TENT.minLevel)
        assertEquals(100, DwellingTier.TENT.coinPrice)
        assertTrue(DwellingTier.SHACK.minLevel > DwellingTier.SHACK.legacyFreeLevel!!)
        assertTrue(DwellingTier.COTTAGE.minLevel > DwellingTier.COTTAGE.legacyFreeLevel!!)
        assertEquals(45, DwellingTier.CASTLE.minLevel)
        assertEquals(70, DwellingTier.GYM.minLevel)
        assertEquals(2000, DwellingTier.GYM.coinPrice)
        assertTrue(DwellingTier.GYM.xpBonusPercent > DwellingTier.CASTLE.xpBonusPercent)
    }

    @Test
    fun characterPose_cyclesCorrectly() {
        var pose = CharacterPose.STANDING
        pose = pose.next()
        assertEquals(CharacterPose.SITTING, pose)

        pose = pose.next()
        assertEquals(CharacterPose.LYING, pose)

        pose = pose.next()
        assertEquals(CharacterPose.STANDING, pose)
    }

    @Test
    fun gymDwelling_visualsAndAnchorsConfigured() {
        assertEquals(com.pixelfitquest.R.drawable.dwelling_gym_landscape, DwellingVisuals.landscapeBgRes(DwellingTier.GYM))
        val standAnchor = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.STANDING, isLandscape = true)
        val sitAnchor = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.SITTING, isLandscape = true)
        org.junit.Assert.assertNotEquals(standAnchor, sitAnchor)
        assertEquals(0.50f, standAnchor.first, 0.01f)
        assertEquals(0.64f, sitAnchor.first, 0.01f)
    }
}
