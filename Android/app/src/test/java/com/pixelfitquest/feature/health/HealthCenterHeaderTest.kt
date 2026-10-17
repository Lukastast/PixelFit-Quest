package com.pixelfitquest.feature.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.pixelfitquest.feature.missions.MissionProgress
import com.pixelfitquest.feature.missions.WeeklyMissionBoard
import com.pixelfitquest.feature.missions.WeeklyMissions
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthMetrics
import com.pixelfitquest.testing.ScreenshotTestBase
import com.pixelfitquest.ui.navigation.CUSTOMIZATION_SCREEN
import com.pixelfitquest.ui.navigation.HEALTH_CENTER_SCREEN
import com.pixelfitquest.ui.navigation.SETTINGS_SCREEN
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.spacing
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

class HealthCenterHeaderTest : ScreenshotTestBase() {

    private fun renderHeaderFrame(
        isCompact: Boolean,
        onOpenSettings: () -> Unit = {},
        onOpenCustomization: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            PixelFitQuestTheme {
                val spacing = MaterialTheme.spacing
                Column(
                    modifier = Modifier.fillMaxSize().background(SlateDeep).padding(
                        horizontal = if (isCompact) spacing.md else spacing.screen,
                        vertical = if (isCompact) spacing.xs else spacing.sm,
                    )
                ) {
                    QuestCenterHeader(
                        coins = 240, level = 14, isCompact = isCompact,
                        onOpenSettings = onOpenSettings, onOpenCustomization = onOpenCustomization,
                    )
                }
            }
        }
    }

    private fun renderHealthCenterFullScreen() {
        val missions = WeeklyMissions.boardForWeek("2026-W42").mapIndexed { index, def ->
            MissionProgress(
                definition = def,
                current = if (index == 0) def.target else def.target / 2,
                claimed = index == 0,
            )
        }
        val board = WeeklyMissionBoard(weekKey = "2026-W42", missions = missions)
        val metrics = HealthMetrics(
            steps = 6_420,
            stepGoal = 8_000,
            weeklySteps = 32_500,
            sleepMinutes = 465,
            weeklyHeartPoints = 110,
            weeklyHeartGoal = 150,
            heartRateBpm = 58,
        )
        composeTestRule.setContent {
            PixelFitQuestTheme {
                Box(modifier = Modifier.fillMaxSize().background(SlateDeep)) {
                    HealthCenterContent(
                        coins = 240,
                        level = 14,
                        section = QuestSection.MISSIONS,
                        board = board,
                        achievementsUnlockedCount = 8,
                        achievementsTotalCount = 24,
                        healthStatus = HealthConnectStatus.AVAILABLE,
                        permissionsGranted = true,
                        metrics = metrics,
                        claims = HealthGoalClaims(steps = false, sleep = true),
                    )
                }
            }
        }
    }

    @Test @Config(qualifiers = QUALIFIER_PORTRAIT)
    fun healthCenterHeader_portrait_screenshotAndCallbacks() {
        var settingsClicked = false
        var customizeClicked = false
        renderHeaderFrame(false, { settingsClicked = true }, { customizeClicked = true })
        captureScreenshot("HealthCenterHeader_portrait")

        composeTestRule.onNodeWithContentDescription("Settings")
            .assertExists().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).performClick()
        assertTrue(settingsClicked)

        composeTestRule.onNodeWithContentDescription("Customize character")
            .assertExists().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).performClick()
        assertTrue(customizeClicked)
    }

    @Test @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun healthCenterHeader_landscape_screenshot() {
        renderHeaderFrame(isCompact = true)
        captureScreenshot("HealthCenterHeader_landscape")

        composeTestRule.onNodeWithContentDescription("Settings")
            .assertExists().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithContentDescription("Customize character")
            .assertExists().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
    }

    @Test @Config(qualifiers = "en-rUS-w360dp-h800dp-xxhdpi")
    fun healthCenterHeader_360dp_screenshotAndNoClipping() {
        renderHeaderFrame(isCompact = false)
        captureScreenshot("HealthCenterHeader_360dp")

        val rootBounds = composeTestRule.onRoot().fetchSemanticsNode().boundsInRoot
        val nodes = listOf(
            composeTestRule.onNodeWithText("Health"),
            composeTestRule.onNodeWithText("Lvl 14"),
            composeTestRule.onNodeWithText("240"),
            composeTestRule.onNodeWithContentDescription("Customize character")
                .assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp),
            composeTestRule.onNodeWithContentDescription("Settings")
                .assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp),
        )
        var prevRight = 0f
        nodes.forEach { interaction ->
            interaction.assertIsDisplayed()
            val b = interaction.fetchSemanticsNode().boundsInRoot
            assertTrue("Expected width > 0 for $b", b.width > 0f)
            assertTrue("Expected left >= 0 and right <= ${rootBounds.width} for $b", b.left >= 0f && b.right <= rootBounds.width)
            assertTrue("Expected non-overlapping horizontal order: $prevRight <= ${b.left}", b.left >= prevRight - 1f)
            prevRight = b.right
        }
    }

    @Test @Config(qualifiers = QUALIFIER_PORTRAIT)
    fun healthCenter_portrait_fullScreenScreenshot() {
        renderHealthCenterFullScreen()
        captureScreenshot("HealthCenter_portrait")
        composeTestRule.onAllNodesWithText("Health").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("WEEKLY MISSIONS").assertIsDisplayed()
    }

    @Test @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun healthCenter_landscape_fullScreenScreenshot() {
        renderHealthCenterFullScreen()
        captureScreenshot("HealthCenter_landscape")
        composeTestRule.onAllNodesWithText("Health").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("WEEKLY MISSIONS").assertIsDisplayed()
    }

    @Test @Config(qualifiers = QUALIFIER_PORTRAIT)
    fun healthCenterHeader_gearNavigatesToSettings_andBackReturnsToHealth() {
        var controller: NavHostController? = null
        composeTestRule.setContent {
            PixelFitQuestTheme {
                val navController = rememberNavController().also { controller = it }
                NavHost(navController = navController, startDestination = HEALTH_CENTER_SCREEN) {
                    composable(HEALTH_CENTER_SCREEN) {
                        QuestCenterHeader(
                            coins = 240, level = 14,
                            onOpenSettings = { navController.navigate(SETTINGS_SCREEN) },
                            onOpenCustomization = { navController.navigate(CUSTOMIZATION_SCREEN) },
                        )
                    }
                    composable(SETTINGS_SCREEN) { Box(Modifier.fillMaxSize()) { Text("Settings Destination", color = PlatinumWhite) } }
                    composable(CUSTOMIZATION_SCREEN) { Box(Modifier.fillMaxSize()) { Text("Customization Destination", color = PlatinumWhite) } }
                }
            }
        }

        composeTestRule.onNodeWithText("Health").assertExists()
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings Destination").assertExists()

        composeTestRule.runOnIdle { controller!!.popBackStack() }
        composeTestRule.onNodeWithText("Health").assertExists()

        composeTestRule.onNodeWithContentDescription("Customize character").performClick()
        composeTestRule.onNodeWithText("Customization Destination").assertExists()

        composeTestRule.runOnIdle { controller!!.popBackStack() }
        composeTestRule.onNodeWithText("Health").assertExists()
    }
}
