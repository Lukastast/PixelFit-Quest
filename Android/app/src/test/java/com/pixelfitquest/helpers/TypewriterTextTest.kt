package com.pixelfitquest.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TypewriterTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `typewriter displays text character by character and calls onComplete`() {
        var onCompleteCalled = false

        composeTestRule.setContent {
            TypewriterText(
                text = "Hello",
                delayMs = 10L, // Use very short delay for testing
                onComplete = { onCompleteCalled = true }
            )
        }

        // Wait for animation to complete
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            onCompleteCalled
        }

        // Should show full text after animation
        composeTestRule.onNodeWithText("Hello").assertExists()
    }

    @Test
    fun `clicking text skips animation and shows full text immediately`() {
        var onCompleteCalled = false

        composeTestRule.setContent {
            TypewriterText(
                text = "Click me",
                delayMs = 200L,
                onComplete = { onCompleteCalled = true }
            )
        }

        composeTestRule.waitForIdle()

        // Wait a bit for first character to appear
        Thread.sleep(50)
        composeTestRule.waitForIdle()

        // Click anywhere on the text
        composeTestRule.onNodeWithText("C", substring = true).performClick()

        // Should immediately show full text
        composeTestRule.onNodeWithText("Click me").assertExists()

        // onComplete should be called
        assert(onCompleteCalled) { "onComplete was not called after click" }
    }

    @Test
    fun `typewriter eventually displays full text`() {
        composeTestRule.setContent {
            TypewriterText(
                text = "ABC",
                delayMs = 50L
            )
        }

        // Just wait for the final result
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            try {
                composeTestRule.onNodeWithText("ABC").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("ABC").assertExists()
    }

    @Test
    fun `typewriter handles empty string`() {
        var onCompleteCalled = false

        composeTestRule.setContent {
            TypewriterText(
                text = "",
                delayMs = 50L,
                onComplete = { onCompleteCalled = true }
            )
        }

        composeTestRule.waitForIdle()

        // Should call onComplete quickly for empty string
        composeTestRule.waitUntil(timeoutMillis = 500) {
            onCompleteCalled
        }

        assert(onCompleteCalled) { "onComplete was not called for empty string" }
    }

    @Test
    fun `LaunchedEffect restarts animation when text changes`() {
        var currentText by mutableStateOf("First")

        composeTestRule.setContent {
            TypewriterText(
                text = currentText,
                delayMs = 10L
            )
        }

        // Wait for first text to complete
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            try {
                composeTestRule.onNodeWithText("First").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Change text
        currentText = "Second"
        composeTestRule.waitForIdle()

        // Wait for new text to complete
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            try {
                composeTestRule.onNodeWithText("Second").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Second").assertExists()
    }
}