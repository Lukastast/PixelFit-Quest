package com.pixelfitquest.helpers

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SnackbarManagerTest {

    @Test
    fun `showMessage emits the message`() = runTest {
        SnackbarManager.snackbarMessages.test {
            // First emission is always the current value (initially null)
            assertNull(awaitItem())

            SnackbarManager.showMessage("Hello World")
            assertEquals("Hello World", awaitItem())

            // Same message again → StateFlow does NOT emit duplicates
            SnackbarManager.showMessage("Hello World")
            expectNoEvents()

            SnackbarManager.showMessage("New Message")
            assertEquals("New Message", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSnackbarState resets to null`() = runTest {
        SnackbarManager.snackbarMessages.test {
            // Skip initial null
            assertNull(awaitItem())

            SnackbarManager.showMessage("Test")
            assertEquals("Test", awaitItem())

            SnackbarManager.clearSnackbarState()
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial value is null`() = runTest {
        SnackbarManager.snackbarMessages.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}