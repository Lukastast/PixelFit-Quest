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
}
