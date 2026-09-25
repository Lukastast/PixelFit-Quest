package com.pixelfitquest.feature.home.model

import com.pixelfitquest.R
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

    @Test
    fun portraitDwellings_shackCottageCastle_visualsAndAnchorsConfigured() {
        assertEquals(com.pixelfitquest.R.drawable.dwelling_shack_portrait, DwellingVisuals.portraitBgRes(DwellingTier.SHACK))
        assertEquals(com.pixelfitquest.R.drawable.dwelling_cottage_portrait, DwellingVisuals.portraitBgRes(DwellingTier.COTTAGE))
        assertEquals(com.pixelfitquest.R.drawable.dwelling_castle_portrait, DwellingVisuals.portraitBgRes(DwellingTier.CASTLE))

        // Shack portrait anchors (bed left, hearth right, rug center)
        val shackLie = DwellingVisuals.characterAnchor(DwellingTier.SHACK, CharacterPose.LYING, isLandscape = false)
        val shackStand = DwellingVisuals.characterAnchor(DwellingTier.SHACK, CharacterPose.STANDING, isLandscape = false)
        assertEquals(0.14f, shackLie.first, 0.01f)
        assertEquals(0.56f, shackLie.second, 0.01f)
        assertEquals(0.50f, shackStand.first, 0.01f)
        assertEquals(0.75f, shackStand.second, 0.01f)

        // Cottage portrait anchors (bed left, fireplace right, rug center)
        val cottageLie = DwellingVisuals.characterAnchor(DwellingTier.COTTAGE, CharacterPose.LYING, isLandscape = false)
        val cottageStand = DwellingVisuals.characterAnchor(DwellingTier.COTTAGE, CharacterPose.STANDING, isLandscape = false)
        assertEquals(0.15f, cottageLie.first, 0.01f)
        assertEquals(0.66f, cottageLie.second, 0.01f)
        assertEquals(0.50f, cottageStand.first, 0.01f)
        assertEquals(0.82f, cottageStand.second, 0.01f)

        // Castle portrait anchors (bed left, grand hearth right, carpet center)
        val castleLie = DwellingVisuals.characterAnchor(DwellingTier.CASTLE, CharacterPose.LYING, isLandscape = false)
        val castleStand = DwellingVisuals.characterAnchor(DwellingTier.CASTLE, CharacterPose.STANDING, isLandscape = false)
        assertEquals(0.15f, castleLie.first, 0.01f)
        assertEquals(0.66f, castleLie.second, 0.01f)
        assertEquals(0.50f, castleStand.first, 0.01f)
        assertEquals(0.82f, castleStand.second, 0.01f)
    }

    @Test
    fun usesRoyalBlanket_onlyCastleUsesRoyalBlanket() {
        assertFalse(DwellingVisuals.usesRoyalBlanket(DwellingTier.TARP))
        assertFalse(DwellingVisuals.usesRoyalBlanket(DwellingTier.TENT))
        assertFalse(DwellingVisuals.usesRoyalBlanket(DwellingTier.SHACK))
        assertFalse(DwellingVisuals.usesRoyalBlanket(DwellingTier.COTTAGE))
        assertTrue(DwellingVisuals.usesRoyalBlanket(DwellingTier.CASTLE))
        assertFalse(DwellingVisuals.usesRoyalBlanket(DwellingTier.GYM))
    }

    @Test
    fun usesWhiteBlanket_cottageAndGymUseWhiteBlanket() {
        // Dwellings 0 (Tarp), 1 (Tent), 2 (Shack) use default blankets
        assertFalse(DwellingVisuals.usesWhiteBlanket(DwellingTier.TARP))
        assertFalse(DwellingVisuals.usesWhiteBlanket(DwellingTier.TENT))
        assertFalse(DwellingVisuals.usesWhiteBlanket(DwellingTier.SHACK))

        // Cottage and Gym use white blanket
        assertTrue(DwellingVisuals.usesWhiteBlanket(DwellingTier.COTTAGE))
        assertTrue(DwellingVisuals.usesWhiteBlanket(DwellingTier.GYM))

        // Castle uses exclusive royal blanket
        assertFalse(DwellingVisuals.usesWhiteBlanket(DwellingTier.CASTLE))
    }

    @Test
    fun lyingSpriteRes_returnsCorrectDrawableForGenderAndDwellingTier() {
        // Male lower tiers
        assertEquals(R.drawable.character_male_lying, DwellingVisuals.lyingSpriteRes("male", DwellingTier.TARP))
        assertEquals(R.drawable.character_male_lying, DwellingVisuals.lyingSpriteRes("male", DwellingTier.TENT))
        assertEquals(R.drawable.character_male_lying, DwellingVisuals.lyingSpriteRes("male", DwellingTier.SHACK))

        // Male cottage & gym (white blanket)
        assertEquals(R.drawable.character_male_lying_white, DwellingVisuals.lyingSpriteRes("male", DwellingTier.COTTAGE))
        assertEquals(R.drawable.character_male_lying_white, DwellingVisuals.lyingSpriteRes("male", DwellingTier.GYM))

        // Male castle (exclusive royal blanket)
        assertEquals(R.drawable.character_male_lying_royal, DwellingVisuals.lyingSpriteRes("male", DwellingTier.CASTLE))

        // Female lower tiers
        assertEquals(R.drawable.character_woman_lying, DwellingVisuals.lyingSpriteRes("female", DwellingTier.TARP))
        assertEquals(R.drawable.character_woman_lying, DwellingVisuals.lyingSpriteRes("woman", DwellingTier.TENT))
        assertEquals(R.drawable.character_woman_lying, DwellingVisuals.lyingSpriteRes("female", DwellingTier.SHACK))

        // Female cottage & gym (white blanket)
        assertEquals(R.drawable.character_woman_lying_white, DwellingVisuals.lyingSpriteRes("female", DwellingTier.COTTAGE))
        assertEquals(R.drawable.character_woman_lying_white, DwellingVisuals.lyingSpriteRes("female", DwellingTier.GYM))

        // Female castle (exclusive royal blanket)
        assertEquals(R.drawable.character_woman_lying_royal, DwellingVisuals.lyingSpriteRes("woman", DwellingTier.CASTLE))
        assertEquals(R.drawable.character_woman_lying_royal, DwellingVisuals.lyingSpriteRes("female", DwellingTier.CASTLE))
    }
}
