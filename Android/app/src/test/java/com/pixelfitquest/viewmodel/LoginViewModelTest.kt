package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.model.service.AuthState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var mockAccountService: AccountService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockAccountService = mockk(relaxed = true)
        viewModel = LoginViewModel(mockAccountService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Email/Password Updates ==========

    @Test
    fun `updateEmail updates email state`() {
        viewModel.updateEmail("test@example.com")

        assertEquals("test@example.com", viewModel.email.value)
    }

    @Test
    fun `updatePassword updates password state`() {
        viewModel.updatePassword("password123")

        assertEquals("password123", viewModel.password.value)
    }

    @Test
    fun `updateEmail resets error state`() = runTest {
        // Set error state
        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)

        // Update email should reset to Idle
        viewModel.updateEmail("test@example.com")

        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    @Test
    fun `updatePassword resets error state`() = runTest {
        // Set error state
        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)

        // Update password should reset to Idle
        viewModel.updatePassword("password123")

        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    // ========== Validation Tests ==========

    @Test
    fun `onLogInClick shows error when email is blank`() = runTest {
        viewModel.updateEmail("")
        viewModel.updatePassword("password123")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onLogInClick shows error when password is blank`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onLogInClick shows error when both fields are blank`() = runTest {
        viewModel.updateEmail("")
        viewModel.updatePassword("")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onLogInClick shows error when email format is invalid`() = runTest {
        viewModel.updateEmail("invalid-email")
        viewModel.updatePassword("password123")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid email format", state.message)
    }

    @Test
    fun `onLogInClick shows error when email has no @ symbol`() = runTest {
        viewModel.updateEmail("testexample.com")
        viewModel.updatePassword("password123")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid email format", state.message)
    }

    @Test
    fun `onLogInClick shows error when email has no domain`() = runTest {
        viewModel.updateEmail("test@")
        viewModel.updatePassword("password123")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid email format", state.message)
    }

    // ========== Successful Login Tests ==========

    @Test
    fun `onLogInClick succeeds with valid credentials`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        coEvery { mockAccountService.signInWithEmail(any(), any()) } just Runs

        var navigationCalled = false
        viewModel.onLogInClick { route, popUp ->
            navigationCalled = true
            assertEquals("home", route)
            assertEquals("login", popUp)
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(navigationCalled)
        assertTrue(viewModel.authState.value is AuthState.Success)
        coVerify { mockAccountService.signInWithEmail("test@example.com", "password123") }
    }

    @Test
    fun `onLogInClick transitions through Loading state`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        coEvery { mockAccountService.signInWithEmail(any(), any()) } just Runs

        val states = mutableListOf<AuthState>()

        // Collect states
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.authState.collect { states.add(it) }
        }

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        job.cancel()

        // Should transition: Idle -> Loading -> Success
        assertTrue(states.contains(AuthState.Idle))
        assertTrue(states.contains(AuthState.Loading))
        assertTrue(states.any { it is AuthState.Success })
    }

    // ========== Login Error Tests ==========

    @Test
    fun `onLogInClick handles network error`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        coEvery { mockAccountService.signInWithEmail(any(), any()) } throws Exception("Network error")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `onLogInClick handles wrong password error`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("wrongpassword")
        coEvery { mockAccountService.signInWithEmail(any(), any()) } throws Exception("Wrong password")

        viewModel.onLogInClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
    }

    @Test
    fun `onLogInClick does not navigate on error`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        coEvery { mockAccountService.signInWithEmail(any(), any()) } throws Exception("Login failed")

        var navigationCalled = false
        viewModel.onLogInClick { _, _ ->
            navigationCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Navigation should NOT be called on error
        assertTrue(!navigationCalled)
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    // ========== State Flow Tests ==========

    @Test
    fun `initial email is empty`() {
        assertEquals("", viewModel.email.value)
    }

    @Test
    fun `initial password is empty`() {
        assertEquals("", viewModel.password.value)
    }

    @Test
    fun `initial authState is Idle`() {
        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    // ========== Google Login Tests (Basic) ==========

    @Test
    fun `onLogInWithGoogle handles error gracefully`() = runTest {
        val mockCredential = mockk<androidx.credentials.Credential>(relaxed = true)
        every { mockCredential.type } returns "unknown_type"

        viewModel.onLogInWithGoogle(mockCredential) { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("Unexpected credential"))
    }

    // Note: Full Google login testing requires mocking Android classes
    // which is complex. The above test covers basic error handling.
}