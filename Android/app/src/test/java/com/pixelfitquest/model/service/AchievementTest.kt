package com.pixelfitquest.model.service.module

import com.pixelfitquest.model.achievementsList
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AchievementTest {

    @Test
    fun `achievementsList contains correct number of achievements`() {
        assertEquals(3, achievementsList.size)
    }

    @Test
    fun `all achievements have unique ids`() {
        val ids = achievementsList.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `achievements are sorted by requiredWorkouts ascending`() {
        val workoutCounts = achievementsList.map { it.requiredWorkouts }
        assertEquals(workoutCounts.sorted(), workoutCounts)
    }

    @Test
    fun `all achievements have non-empty names and descriptions`() {
        achievementsList.forEach { achievement ->
            assertTrue(achievement.name.isNotBlank(), "Achievement ${achievement.id} has blank name")
            assertTrue(achievement.description.isNotBlank(), "Achievement ${achievement.id} has blank description")
        }
    }

    @Test
    fun `all achievements have positive requiredWorkouts`() {
        achievementsList.forEach { achievement ->
            assertTrue(achievement.requiredWorkouts > 0, "Achievement ${achievement.id} has invalid requiredWorkouts")
        }
    }

    @Test
    fun `rookie achievement requires 1 workout`() {
        val rookie = achievementsList.find { it.id == "bronze_workout_1" }
        assertEquals(1, rookie?.requiredWorkouts)
    }

    @Test
    fun `veteran achievement requires 10 workouts`() {
        val veteran = achievementsList.find { it.id == "silver_workout_10" }
        assertEquals(10, veteran?.requiredWorkouts)
    }

    @Test
    fun `legend achievement requires 50 workouts`() {
        val legend = achievementsList.find { it.id == "gold_workout_50" }
        assertEquals(50, legend?.requiredWorkouts)
    }
}