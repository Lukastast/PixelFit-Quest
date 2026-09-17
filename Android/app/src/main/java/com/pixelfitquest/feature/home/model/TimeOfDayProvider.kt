package com.pixelfitquest.feature.home.model

import java.time.LocalTime

object TimeOfDayProvider {
    /**
     * Determines the natural character activity pose based on the time of day:
     * - 22:00 – 06:59: LYING (Sleeping peacefully in bed)
     * - 07:00 – 13:59: STANDING (Active morning, ready for quests & workouts)
     * - 14:00 – 21:59: SITTING (Relaxing in dwelling)
     */
    fun getDefaultPoseForHour(hour: Int): CharacterPose {
        return when (hour) {
            in 7..13 -> CharacterPose.STANDING
            in 14..21 -> CharacterPose.SITTING
            else -> CharacterPose.LYING
        }
    }

    fun getDefaultPoseForCurrentTime(): CharacterPose {
        return getDefaultPoseForHour(LocalTime.now().hour)
    }
}
