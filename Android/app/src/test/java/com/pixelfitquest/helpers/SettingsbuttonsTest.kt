package com.pixelfitquest.helpers

import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class SettingsbuttonsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displayNameCard_rendersCorrectlyAndOpensDialogOnClick() {
        val onUpdate = mockk<(String) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            DisplayNameCard(displayName = "TestUser", onUpdateDisplayNameClick = onUpdate)
        }

        // 1. Card shows the user's name
        composeTestRule.onNodeWithText("TestUser").assertIsDisplayed()

        // 2. Click the edit icon
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        // 3. Dialog title = "Display name"
        composeTestRule.onNodeWithText("Display name").assertIsDisplayed()

        // 4. Click the "Update" button (NOT "UPDATE" — PixelArtButton does NOT uppercase)
        composeTestRule.onNodeWithText("Update").performClick()

        // 5. Callback was called with current text
        verify { onUpdate("TestUser") }
    }

    @Test
    fun displayNameCard_handlesBlankName() {
        val onUpdate = mockk<(String) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            DisplayNameCard(displayName = "", onUpdateDisplayNameClick = onUpdate)
        }

        // Correct string from strings.xml: "Display name" (capital D, lowercase n)
        composeTestRule.onNodeWithText("Display name").assertIsDisplayed()
    }

    @Test
    fun accountCenterCard_rendersAndTriggersClick() {
        val onClick = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            AccountCenterCard(title = "Test Card", icon = androidx.compose.material.icons.Icons.Default.Link, onCardClick = onClick)
        }

        composeTestRule.onNodeWithText("Test Card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Card").performClick()

        verify { onClick() }
    }

    @Test
    fun exitAppCard_rendersAndOpensDialogOnClick() {
        val onSignOut = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            ExitAppCard(onSignOutClick = onSignOut)
        }

        composeTestRule.onNodeWithText("Sign out").assertIsDisplayed()

        composeTestRule.onNodeWithText("Sign out").performClick()

        composeTestRule.onNodeWithText("Sign out?").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("Sign out")[1]
            .assertIsDisplayed()
            .performClick()

        // 5. Callback was called
        verify { onSignOut() }
    }

    @Test
    fun googleLinkCard_rendersAndOpensDialogOnClick() {
        val onLink = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            GoogleLinkCard(user = null, onLinkClick = onLink)
        }

        // 1. The card itself is clickable and has the text as its content description
        //    (Material3 Card merges text into the clickable node)
        composeTestRule
            .onNodeWithText("Link Google account")
            .assertExists()  // ← Just check it exists (not displayed as Text node)
            .performClick()

        // 2. Dialog appears
        composeTestRule.onNodeWithText("Link Google account?").assertIsDisplayed()

        // 3. Click the "Link Account" button (this one IS a real Text node)
        composeTestRule
            .onNodeWithText("Link Account")
            .assertIsDisplayed()
            .performClick()

        // 4. Callback was called
        verify { onLink() }
    }

    @Test
    fun removeAccountCard_rendersAndOpensDialogOnClick() {
        val onRemove = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            RemoveAccountCard(onRemoveAccountClick = onRemove)
        }

        // 1. Card shows "Delete" (from R.string.delete_account)
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()

        // 2. Click the delete icon (content description = "Delete")
        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // 3. Dialog title = "Delete account?"
        composeTestRule.onNodeWithText("Delete account?").assertIsDisplayed()

        // 4. Confirm button also says "Delete" → two nodes exist → pick the second one
        composeTestRule.onAllNodesWithText("Delete")[1].performClick()

        // 5. Callback was called
        verify { onRemove() }
    }

    @Test
    fun volumeCard_rendersAndAdjustsVolumeOnClick() {
        val onVolumeChange = mockk<(Int) -> Unit>(relaxed = true)
        val initialVolume = 50

        composeTestRule.setContent {
            VolumeCard(musicVolume = initialVolume, onVolumeChange = onVolumeChange)
        }

        composeTestRule.onNodeWithText("Volume").assertIsDisplayed()
        composeTestRule.onNodeWithText("50%").assertIsDisplayed()

        // Increase
        composeTestRule.onNodeWithContentDescription("Increase Volume").performClick()
        verify { onVolumeChange(60) }

        // Decrease
        composeTestRule.onNodeWithContentDescription("Decrease Volume").performClick()
        verify { onVolumeChange(40) }
    }

    @Test
    fun volumeCard_capsAtMinAndMax() {
        val onVolumeChange = mockk<(Int) -> Unit>(relaxed = true)

        // Use a single setContent with mutable state
        var volume by mutableStateOf(0)

        composeTestRule.setContent {
            VolumeCard(musicVolume = volume, onVolumeChange = { volume = it; onVolumeChange(it) })
        }

        // Test at minimum (0)
        composeTestRule.onNodeWithContentDescription("Decrease Volume").performClick()
        verify(exactly = 1) { onVolumeChange(0) }  // Stays at 0

        // Update volume to 100
        volume = 100
        composeTestRule.waitForIdle()  // Let Compose recompose

        // Test at maximum (100)
        composeTestRule.onNodeWithContentDescription("Increase Volume").performClick()
        verify(exactly = 1) { onVolumeChange(100) }  // Stays at 100
    }
}