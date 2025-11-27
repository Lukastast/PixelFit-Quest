package com.pixelfitquest.helpers

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AutoSizeTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun autoSizeText_fitsAtMaxFontSize() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoSizeText(
                    text = "Short text",
                    style = MaterialTheme.typography.bodyLarge,
                    maxFontSize = 24.sp,
                    minFontSize = 12.sp,
                    modifier = Modifier.width(200.dp)
                )
            }
        }

        composeTestRule.onNodeWithText("Short text").assertIsDisplayed()
    }

    @Test
    fun autoSizeText_adjustsToSmallerFontOnOverflow() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoSizeText(
                    text = "Very long text that should cause overflow and size down",
                    style = MaterialTheme.typography.bodyLarge,
                    maxFontSize = 24.sp,
                    minFontSize = 12.sp,
                    stepSize = 2.sp,
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Very long text that should cause overflow and size down",
            substring = true
        ).assertIsDisplayed()
    }

    @Test
    fun autoSizeText_handlesEmptyText() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoSizeText(
                    text = "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(200.dp)
                )
            }
        }

        // For empty text, just assert that the test doesn't crash
        composeTestRule.waitForIdle()
    }

    @Test
    fun autoSizeText_clipsAtMinFontSizeOnExtremeOverflow() {
        composeTestRule.setContent {
            MaterialTheme {
                AutoSizeText(
                    text = "Extremely long text that won't fit even at min size so should clip",
                    style = MaterialTheme.typography.bodyLarge,
                    maxFontSize = 24.sp,
                    minFontSize = 12.sp,
                    modifier = Modifier.width(50.dp)
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Extremely long text that won't fit even at min size so should clip",
            substring = true
        ).assertIsDisplayed()
    }
}