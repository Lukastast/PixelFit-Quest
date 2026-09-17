package com.pixelfitquest.feature.home.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeOfDayProviderTest {

    @Test
    fun getDefaultPoseForHour_morningReturnsStanding() {
        assertEquals(CharacterPose.STANDING, TimeOfDayProvider.getDefaultPoseForHour(7))
        assertEquals(CharacterPose.STANDING, TimeOfDayProvider.getDefaultPoseForHour(10))
        assertEquals(CharacterPose.STANDING, TimeOfDayProvider.getDefaultPoseForHour(13))
    }

    @Test
    fun getDefaultPoseForHour_afternoonReturnsSitting() {
        assertEquals(CharacterPose.SITTING, TimeOfDayProvider.getDefaultPoseForHour(14))
        assertEquals(CharacterPose.SITTING, TimeOfDayProvider.getDefaultPoseForHour(18))
        assertEquals(CharacterPose.SITTING, TimeOfDayProvider.getDefaultPoseForHour(21))
    }

    @Test
    fun getDefaultPoseForHour_nightReturnsLying() {
        assertEquals(CharacterPose.LYING, TimeOfDayProvider.getDefaultPoseForHour(22))
        assertEquals(CharacterPose.LYING, TimeOfDayProvider.getDefaultPoseForHour(23))
        assertEquals(CharacterPose.LYING, TimeOfDayProvider.getDefaultPoseForHour(0))
        assertEquals(CharacterPose.LYING, TimeOfDayProvider.getDefaultPoseForHour(3))
        assertEquals(CharacterPose.LYING, TimeOfDayProvider.getDefaultPoseForHour(6))
    }
}
