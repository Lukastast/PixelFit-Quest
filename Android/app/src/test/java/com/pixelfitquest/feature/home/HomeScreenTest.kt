package com.pixelfitquest.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.home.model.CharacterPose
import com.pixelfitquest.feature.home.model.HomeDisplayState
import com.pixelfitquest.feature.home.model.LastDone
import com.pixelfitquest.feature.home.model.LastDoneDay
import com.pixelfitquest.feature.home.model.WeeklyStats
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.testing.ScreenshotTestBase
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config
import java.time.DayOfWeek

class HomeScreenTest : ScreenshotTestBase() {

    private val sampleDisplayState = HomeDisplayState(
        nextUpTemplate = WorkoutTemplate(
            id = "t_push_day",
            name = "Push Day",
            plan = WorkoutPlan(
                items = listOf(
                    WorkoutPlanItem(exercise = ExerciseType.BENCH_PRESS, sets = 4, weight = 80f),
                    WorkoutPlanItem(exercise = ExerciseType.OVERHEAD_PRESS, sets = 3, weight = 50f),
                )
            )
        ),
        lastDone = LastDone.Done(LastDoneDay.Weekday(DayOfWeek.THURSDAY), 52),
        weeklyStats = WeeklyStats(workoutsCount = 3, setsCount = 62, kmRun = null),
    )

    private fun renderHomeScreen(
        isLandscape: Boolean,
        onStartClicked: () -> Unit = {},
        onChooseAnotherClicked: () -> Unit = {},
        onStreakClicked: () -> Unit = {},
        onLevelClicked: () -> Unit = {},
        onMissionsClicked: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            PixelFitQuestTheme {
                HomeScreenContent(
                    displayState = sampleDisplayState,
                    streak = 6,
                    displayLevel = "14",
                    xpProgressFraction = 0.65f,
                    characterData = CharacterData(gender = "male", variant = "basic"),
                    characterPose = CharacterPose.LYING,
                    isLandscape = isLandscape,
                    onPoseCycle = {},
                    onStreakClick = onStreakClicked,
                    onLevelClick = onLevelClicked,
                    onStartWorkout = { _, _ -> onStartClicked() },
                    onChooseAnotherTemplate = onChooseAnotherClicked,
                    onMissionsClick = onMissionsClicked,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    @Test
    @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun homeScreen_landscape_fullScreenScreenshot() {
        renderHomeScreen(isLandscape = true)
        captureScreenshot("HomeScreen_landscape")

        composeTestRule.onNodeWithText("WEEK STREAK 6").assertExists()
        composeTestRule.onNodeWithText("LV 14").assertExists()
        composeTestRule.onNodeWithText("MISSIONS").assertExists()
        composeTestRule.onNodeWithText("NEXT UP").assertExists()
        composeTestRule.onNodeWithText("PUSH DAY").assertExists()
        composeTestRule.onNodeWithText("Last done Thu, 52 min").assertExists()
        composeTestRule.onNodeWithText("START").assertExists()
        composeTestRule.onNodeWithText("Choose another template").assertExists()
        composeTestRule.onNodeWithText("THIS WEEK").assertExists()
        composeTestRule.onNodeWithText("3").assertExists()
        composeTestRule.onNodeWithText("WORKOUTS").assertExists()
        composeTestRule.onNodeWithText("62").assertExists()
        composeTestRule.onNodeWithText("SETS").assertExists()
    }

    @Test
    @Config(qualifiers = QUALIFIER_PORTRAIT)
    fun homeScreen_portrait_fullScreenScreenshot() {
        renderHomeScreen(isLandscape = false)
        captureScreenshot("HomeScreen_portrait")

        composeTestRule.onNodeWithText("WEEK STREAK 6").assertExists()
        composeTestRule.onNodeWithText("LV 14").assertExists()
        composeTestRule.onNodeWithText("MISSIONS").assertExists()
        composeTestRule.onNodeWithText("NEXT UP").assertExists()
        composeTestRule.onNodeWithText("PUSH DAY").assertExists()
        composeTestRule.onNodeWithText("START").assertExists()
        composeTestRule.onNodeWithText("Choose another template").assertExists()
        composeTestRule.onNodeWithText("THIS WEEK").assertExists()
    }

    @Test
    @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun homeScreen_interactions_triggerCallbacks() {
        var startClicked = false
        var chooseAnotherClicked = false
        var streakClicked = false
        var levelClicked = false
        var missionsClicked = false

        renderHomeScreen(
            isLandscape = true,
            onStartClicked = { startClicked = true },
            onChooseAnotherClicked = { chooseAnotherClicked = true },
            onStreakClicked = { streakClicked = true },
            onLevelClicked = { levelClicked = true },
            onMissionsClicked = { missionsClicked = true },
        )

        composeTestRule.onNodeWithText("START").performClick()
        assertTrue(startClicked)

        composeTestRule.onNodeWithText("Choose another template").performClick()
        assertTrue(chooseAnotherClicked)

        composeTestRule.onNodeWithText("WEEK STREAK 6").performClick()
        assertTrue(streakClicked)

        composeTestRule.onNodeWithText("LV 14").performClick()
        assertTrue(levelClicked)

        composeTestRule.onNodeWithText("MISSIONS").performClick()
        assertTrue(missionsClicked)
    }
}
