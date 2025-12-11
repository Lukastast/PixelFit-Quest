package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.User
import com.pixelfitquest.model.UserData
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.R
import com.pixelfitquest.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var mockAccountService: AccountService
    private lateinit var mockUserRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockAccountService = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)

        // Default mocks
        coEvery { mockAccountService.getUserProfile() } returns User()
        coEvery { mockUserRepository.getUserData() } returns flowOf(
            UserData(height = 175, musicVolume = 50)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Initialization Tests ==========

    @Test
    fun `init loads user profile`() = runTest {
        val testUser = User(id = "123", email = "test@example.com", displayName = "Test User")
        coEvery { mockAccountService.getUserProfile() } returns testUser

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("123", viewModel.user.value.id)
        assertEquals("test@example.com", viewModel.user.value.email)
        assertEquals("Test User", viewModel.user.value.displayName)
    }

    @Test
    fun `init loads user settings`() = runTest {
        val testSettings = UserData(height = 180, musicVolume = 75)
        coEvery { mockUserRepository.getUserData() } returns flowOf(testSettings)

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(180, viewModel.userData.value?.height)
        assertEquals(75, viewModel.userData.value?.musicVolume)
    }

    // ========== Display Name Update Tests ==========

    @Test
    fun `onUpdateDisplayNameClick updates display name`() = runTest {
        val initialUser = User(id = "123", displayName = "Old Name")
        val updatedUser = User(id = "123", displayName = "New Name")

        coEvery { mockAccountService.getUserProfile() } returnsMany listOf(initialUser, updatedUser)
        coEvery { mockAccountService.updateDisplayName(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onUpdateDisplayNameClick("New Name")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockAccountService.updateDisplayName("New Name") }
        assertEquals("New Name", viewModel.user.value.displayName)
    }

    @Test
    fun `onUpdateDisplayNameClick handles empty name`() = runTest {
        coEvery { mockAccountService.updateDisplayName(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onUpdateDisplayNameClick("")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockAccountService.updateDisplayName("") }
    }

    // ========== Profile Picture Tests ==========

    @Test
    fun `getProfilePictureModel returns local resource when URL is null`() = runTest {
        val user = User(profilePictureUrl = null)
        coEvery { mockAccountService.getUserProfile() } returns user

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.getProfilePictureModel()

        assertEquals(R.mipmap.app_icon_round, result)
    }

    @Test
    fun `getProfilePictureModel returns local resource when URL is blank`() = runTest {
        val user = User(profilePictureUrl = "")
        coEvery { mockAccountService.getUserProfile() } returns user

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.getProfilePictureModel()

        assertEquals(R.mipmap.app_icon_round, result)
    }

    @Test
    fun `getProfilePictureModel returns URL when URL is present`() = runTest {
        val user = User(profilePictureUrl = "https://example.com/profile.jpg")
        coEvery { mockAccountService.getUserProfile() } returns user

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.getProfilePictureModel()

        assertEquals("https://example.com/profile.jpg", result)
    }

    @Test
    fun `getProfilePictureModel returns local resource when URL is whitespace`() = runTest {
        val user = User(profilePictureUrl = "   ")
        coEvery { mockAccountService.getUserProfile() } returns user

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.getProfilePictureModel()

        assertEquals(R.mipmap.app_icon_round, result)
    }

    // ========== Sign Out Tests ==========

    @Test
    fun `onSignOutClick calls signOut and restarts app`() = runTest {
        coEvery { mockAccountService.signOut() } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        var restartCalled = false
        var restartRoute = ""

        viewModel.onSignOutClick { route ->
            restartCalled = true
            restartRoute = route
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(restartCalled)
        assertEquals("SplashScreen", restartRoute)  // Changed from "splash"
        coVerify { mockAccountService.signOut() }
    }

    @Test
    fun `onDeleteAccountClick calls deleteAccount and restarts app`() = runTest {
        coEvery { mockAccountService.deleteAccount() } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        var restartCalled = false
        var restartRoute = ""

        viewModel.onDeleteAccountClick { route ->
            restartCalled = true
            restartRoute = route
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(restartCalled)
        assertEquals("SplashScreen", restartRoute)  // Changed from "splash"
        coVerify { mockAccountService.deleteAccount() }
    }

    // ========== Height Settings Tests ==========

    @Test
    fun `setHeight updates height setting`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setHeight(180)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserData(match {
                it["height"] == 180
            })
        }
    }

    @Test
    fun `setHeight handles minimum valid height`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setHeight(1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserData(match {
                it["height"] == 1
            })
        }
    }

    @Test
    fun `setHeight handles maximum valid height`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setHeight(272)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserData(match {
                it["height"] == 272
            })
        }
    }

    @Test
    fun `setHeight sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } throws Exception("Update failed")

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setHeight(180)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Update failed"))
    }

    // ========== Music Volume Settings Tests ==========

    @Test
    fun `setMusicVolume updates music volume setting`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setMusicVolume(75)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserData(match {
                it["musicVolume"] == 75
            })
        }
    }

    @Test
    fun `setMusicVolume handles zero volume`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setMusicVolume(0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserData(match {
                it["musicVolume"] == 0
            })
        }
    }

    @Test
    fun `setMusicVolume handles maximum volume`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } just Runs

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setMusicVolume(100)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserData(match {
                it["musicVolume"] == 100
            })
        }
    }

    @Test
    fun `setMusicVolume sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.updateUserData(any()) } throws Exception("Volume update failed")

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setMusicVolume(50)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Volume update failed"))
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `loadUserData sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.getUserData() } throws Exception("Network error")

        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Network error"))
    }

    // ========== State Flow Tests ==========

    @Test
    fun `initial user is empty`() = runTest {
        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)

        // Before initialization completes
        assertEquals("", viewModel.user.value.id)
    }

    @Test
    fun `initial error is null`() = runTest {
        viewModel = SettingsViewModel(mockAccountService, mockUserRepository)

        assertEquals(null, viewModel.error.value)
    }
}