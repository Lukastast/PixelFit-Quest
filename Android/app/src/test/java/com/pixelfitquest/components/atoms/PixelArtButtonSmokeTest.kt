package com.pixelfitquest.components.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.pixelfitquest.R
import com.pixelfitquest.testing.ScreenshotTestBase
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

class PixelArtButtonSmokeTest : ScreenshotTestBase() {

    private fun runSmokeTest(screenshotName: String) {
        var clicked = false
        composeTestRule.setContent {
            PixelFitQuestTheme {
                Box(modifier = Modifier.padding(16.dp)) {
                    PixelArtButton(
                        onClick = { clicked = true },
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier.size(width = 160.dp, height = 56.dp),
                    ) {
                        Text(text = "START", color = Color.White)
                    }
                }
            }
        }
        captureScreenshot(screenshotName)
        composeTestRule.onNodeWithContentDescription("Button").assertExists().performClick()
        assertTrue(clicked)
    }

    @Test
    @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun pixelArtButton_landscape_rendersAndClicks() = runSmokeTest("PixelArtButton_landscape")

    @Test
    @Config(qualifiers = QUALIFIER_PORTRAIT)
    fun pixelArtButton_portrait_rendersAndClicks() = runSmokeTest("PixelArtButton_portrait")
}
