package com.pixelfitquest.ui.navigation

import com.pixelfitquest.helpers.CUSTOMIZATION_SCREEN
import com.pixelfitquest.helpers.HOME_SCREEN
import com.pixelfitquest.helpers.INTRO_SCREEN
import com.pixelfitquest.helpers.LOGIN_SCREEN
import com.pixelfitquest.helpers.SETTINGS_SCREEN
import com.pixelfitquest.helpers.SIGNUP_SCREEN
import com.pixelfitquest.helpers.SPLASH_SCREEN
import com.pixelfitquest.helpers.WORKOUT_CUSTOMIZATION_SCREEN
import com.pixelfitquest.helpers.WORKOUT_SCREEN
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for AppScaffold utility functions.
 *
 * Note: The main AppScaffold composable is not unit-testable as it requires
 * composition context, navigation, lifecycle, and Android framework components.
 * These tests cover the extracted business logic that can be tested in isolation.
 */
class AppScaffoldUtilsTest {

    // ========== Bottom Bar Visibility Logic ==========

    @Test
    fun `shouldShowBottomBar returns true when user is logged in and on home screen`() {
        assertTrue(shouldShowBottomBar(HOME_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns true when user is logged in and on workout screen`() {
        assertTrue(shouldShowBottomBar(WORKOUT_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns true when user is logged in and on customization screen`() {
        assertTrue(shouldShowBottomBar(CUSTOMIZATION_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns true when user is logged in and on settings screen`() {
        assertTrue(shouldShowBottomBar(SETTINGS_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns true when user is logged in and on workout customization screen`() {
        assertTrue(shouldShowBottomBar(WORKOUT_CUSTOMIZATION_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns false when user is not logged in`() {
        assertFalse(shouldShowBottomBar(HOME_SCREEN, isUserLoggedIn = false))
    }

    @Test
    fun `shouldShowBottomBar returns false when on login screen even if logged in`() {
        assertFalse(shouldShowBottomBar(LOGIN_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns false when on signup screen even if logged in`() {
        assertFalse(shouldShowBottomBar(SIGNUP_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns false when on splash screen`() {
        assertFalse(shouldShowBottomBar(SPLASH_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns false when on intro screen`() {
        assertFalse(shouldShowBottomBar(INTRO_SCREEN, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns false when route is null`() {
        assertFalse(shouldShowBottomBar(null, isUserLoggedIn = true))
    }

    @Test
    fun `shouldShowBottomBar returns false when route is unknown`() {
        assertFalse(shouldShowBottomBar("unknown_screen", isUserLoggedIn = true))
    }

    // ========== Music Volume Calculation ==========

    @Test
    fun `calculateMusicVolume converts 0 percent to 0 float`() {
        assertEquals(0.0f, calculateMusicVolume(0))
    }

    @Test
    fun `calculateMusicVolume converts 50 percent to 0_5 float`() {
        assertEquals(0.5f, calculateMusicVolume(50))
    }

    @Test
    fun `calculateMusicVolume converts 100 percent to 1 float`() {
        assertEquals(1.0f, calculateMusicVolume(100))
    }

    @Test
    fun `calculateMusicVolume converts 25 percent to 0_25 float`() {
        assertEquals(0.25f, calculateMusicVolume(25))
    }

    @Test
    fun `calculateMusicVolume converts 75 percent to 0_75 float`() {
        assertEquals(0.75f, calculateMusicVolume(75))
    }

    @Test
    fun `calculateMusicVolume handles 1 percent`() {
        assertEquals(0.01f, calculateMusicVolume(1))
    }

    @Test
    fun `calculateMusicVolume handles 99 percent`() {
        assertEquals(0.99f, calculateMusicVolume(99))
    }

    // ========== Workout Route Building ==========

    @Test
    fun `buildWorkoutRoute creates correct route with plan and template name`() {
        val planJson = """{"items":[{"exercise":"BENCH_PRESS","sets":3,"weight":100.0}]}"""
        val templateName = "Upper Body"

        val route = buildWorkoutRoute(planJson, templateName)

        assertEquals("$WORKOUT_SCREEN/$planJson/$templateName", route)
    }

    @Test
    fun `buildWorkoutRoute handles empty template name`() {
        val planJson = """{"items":[]}"""
        val templateName = ""

        val route = buildWorkoutRoute(planJson, templateName)

        assertEquals("$WORKOUT_SCREEN/$planJson/$templateName", route)
    }

    @Test
    fun `buildWorkoutRoute handles special characters in template name`() {
        val planJson = """{"items":[]}"""
        val templateName = "Upper Body & Core"

        val route = buildWorkoutRoute(planJson, templateName)

        assertTrue(route.contains("Upper Body & Core"))
    }

    // ========== Workout Resume Route Building ==========

    @Test
    fun `buildWorkoutResumeRoute creates correct route`() {
        val workoutId = "workout-123"

        val route = buildWorkoutResumeRoute(workoutId)

        assertEquals("workout_resume/$workoutId", route)
    }

    @Test
    fun `buildWorkoutResumeRoute handles empty workout id`() {
        val route = buildWorkoutResumeRoute("")

        assertEquals("workout_resume/", route)
    }
}

// ========== Extracted Utility Functions ==========
// Add these to a separate AppScaffoldUtils.kt file for better testability

/**
 * Determines if the bottom navigation bar should be shown based on current route and login state.
 */
fun shouldShowBottomBar(currentRoute: String?, isUserLoggedIn: Boolean): Boolean {
    return isUserLoggedIn && currentRoute in listOf(
        HOME_SCREEN,
        WORKOUT_SCREEN,
        CUSTOMIZATION_SCREEN,
        SETTINGS_SCREEN,
        WORKOUT_CUSTOMIZATION_SCREEN
    )
}

/**
 * Converts a percentage volume (0-100) to a float volume (0.0-1.0) for MediaPlayer.
 */
fun calculateMusicVolume(settingsVolume: Int): Float {
    return settingsVolume / 100f
}

/**
 * Builds the workout screen route with plan JSON and template name.
 */
fun buildWorkoutRoute(planJson: String, templateName: String): String {
    return "$WORKOUT_SCREEN/$planJson/$templateName"
}

/**
 * Builds the workout resume screen route with workout ID.
 */
fun buildWorkoutResumeRoute(workoutId: String): String {
    return "workout_resume/$workoutId"
}