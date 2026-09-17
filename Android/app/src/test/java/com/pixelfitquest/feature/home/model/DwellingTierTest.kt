package com.pixelfitquest.feature.home.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DwellingTierTest {

    @Test
    fun forLevel_returnsCorrectTierAcrossBoundaries() {
        assertEquals(DwellingTier.TARP, DwellingTier.forLevel(1))
        assertEquals(DwellingTier.TARP, DwellingTier.forLevel(4))

        assertEquals(DwellingTier.TENT, DwellingTier.forLevel(5))
        assertEquals(DwellingTier.TENT, DwellingTier.forLevel(9))

        assertEquals(DwellingTier.SHACK, DwellingTier.forLevel(10))
        assertEquals(DwellingTier.SHACK, DwellingTier.forLevel(14))

        assertEquals(DwellingTier.COTTAGE, DwellingTier.forLevel(15))
        assertEquals(DwellingTier.COTTAGE, DwellingTier.forLevel(24))

        assertEquals(DwellingTier.CASTLE, DwellingTier.forLevel(25))
        assertEquals(DwellingTier.CASTLE, DwellingTier.forLevel(30))
    }

    @Test
    fun forLevel_zeroOrNegative_fallsBackToTarp() {
        assertEquals(DwellingTier.TARP, DwellingTier.forLevel(0))
        assertEquals(DwellingTier.TARP, DwellingTier.forLevel(-5))
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
    fun gymDwelling_notUnlockedViaLevelProgression() {
        assertEquals("dwelling_gym", DwellingTier.GYM.id)
        for (level in 1..50) {
            org.junit.Assert.assertNotEquals(DwellingTier.GYM, DwellingTier.forLevel(level))
        }
    }

    @Test
    fun gymDwelling_visualsAndAnchorsConfigured() {
        assertEquals(com.pixelfitquest.R.drawable.dwelling_gym_landscape, DwellingVisuals.landscapeBgRes(DwellingTier.GYM))
        val standAnchor = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.STANDING, isLandscape = true)
        val sitAnchor = DwellingVisuals.characterAnchor(DwellingTier.GYM, CharacterPose.SITTING, isLandscape = true)
        org.junit.Assert.assertNotEquals(standAnchor, sitAnchor)
        assertEquals(0.32f, standAnchor.first, 0.01f)
        assertEquals(0.64f, sitAnchor.first, 0.01f)
    }
}
