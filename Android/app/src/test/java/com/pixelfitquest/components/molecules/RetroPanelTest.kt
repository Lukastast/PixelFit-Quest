package com.pixelfitquest.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.pixelfitquest.testing.ScreenshotTestBase
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

class RetroPanelTest : ScreenshotTestBase() {

    @Test
    @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun retroPanel_screenshot_andPillClicks() {
        var pillClicked = false
        composeTestRule.setContent {
            PixelFitQuestTheme {
                Box(modifier = Modifier.padding(16.dp).testTag("panel")) {
                    RetroPanel(modifier = Modifier.width(320.dp)) {
                        RetroPill(
                            onClick = { pillClicked = true },
                            modifier = Modifier.testTag("pill"),
                        ) {
                            Text(text = "WEEK STREAK 6")
                        }
                        RetroStatBox(
                            value = "3",
                            label = "WORKOUTS",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        captureScreenshot("RetroPanel_landscape", composeTestRule.onNodeWithTag("panel"))

        composeTestRule.onNodeWithText("WEEK STREAK 6").assertExists().performClick()
        assertTrue(pillClicked)
        composeTestRule.onNodeWithText("3").assertExists()
        composeTestRule.onNodeWithText("WORKOUTS").assertExists()
    }
}
