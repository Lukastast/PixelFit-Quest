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

        composeTestRule.onNodeWithText("Quest Center").assertExists()
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings Destination").assertExists()

        composeTestRule.runOnIdle { controller!!.popBackStack() }
        composeTestRule.onNodeWithText("Quest Center").assertExists()

        composeTestRule.onNodeWithContentDescription("Customize character").performClick()
        composeTestRule.onNodeWithText("Customization Destination").assertExists()

        composeTestRule.runOnIdle { controller!!.popBackStack() }
        composeTestRule.onNodeWithText("Quest Center").assertExists()
    }
}
