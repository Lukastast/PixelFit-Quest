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
class SignupViewModelTest {

    private lateinit var viewModel: SignupViewModel
    private lateinit var mockAccountService: AccountService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockAccountService = mockk(relaxed = true)
        viewModel = SignupViewModel(mockAccountService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Input Updates ==========

    @Test
    fun `updateEmail updates email state`() {
        viewModel.updateEmail("test@example.com")
        assertEquals("test@example.com", viewModel.email.value)
    }

    @Test
    fun `updatePassword updates password state`() {
        viewModel.updatePassword("Password1")
        assertEquals("Password1", viewModel.password.value)
    }

    @Test
    fun `updateConfirmPassword updates confirmPassword state`() {
        viewModel.updateConfirmPassword("Password1")
        assertEquals("Password1", viewModel.confirmPassword.value)
    }

    @Test
    fun `updateEmail resets error state`() = runTest {
        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)

        viewModel.updateEmail("test@example.com")
        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    @Test
    fun `updatePassword resets error state`() = runTest {
        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)

        viewModel.updatePassword("Password1")
        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    @Test
    fun `updateConfirmPassword resets error state`() = runTest {
        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)

        viewModel.updateConfirmPassword("Password1")
        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    // ========== Blank Field Validation ==========

    @Test
    fun `onSignUpClick shows error when email is blank`() = runTest {
        viewModel.updateEmail("")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onSignUpClick shows error when password is blank`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("")
        viewModel.updateConfirmPassword("Password1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onSignUpClick shows error when confirmPassword is blank`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onSignUpClick shows error when all fields are blank`() = runTest {
        viewModel.updateEmail("")
        viewModel.updatePassword("")
        viewModel.updateConfirmPassword("")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    // ========== Email Format Validation ==========

    @Test
    fun `onSignUpClick shows error when email format is invalid`() = runTest {
        viewModel.updateEmail("invalid-email")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid email format", state.message)
    }

    @Test
    fun `onSignUpClick shows error when email has no @ symbol`() = runTest {
        viewModel.updateEmail("testexample.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid email format", state.message)
    }

    // ========== Password Strength Validation ==========

    @Test
    fun `onSignUpClick shows error when password is too short`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Pass1")  // Only 5 characters
        viewModel.updateConfirmPassword("Pass1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("at least 6 characters"))
    }

    @Test
    fun `onSignUpClick shows error when password has no digit`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password")  // No digit
        viewModel.updateConfirmPassword("Password")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("1 digit"))
    }

    @Test
    fun `onSignUpClick shows error when password has no lowercase`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("PASSWORD1")  // No lowercase
        viewModel.updateConfirmPassword("PASSWORD1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("1 lowercase"))
    }

    @Test
    fun `onSignUpClick shows error when password has no uppercase`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password1")  // No uppercase
        viewModel.updateConfirmPassword("password1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("1 uppercase"))
    }

    @Test
    fun `onSignUpClick shows error when password is all lowercase`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password")
        viewModel.updateConfirmPassword("password")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
    }

    // ========== Password Confirmation Validation ==========

    @Test
    fun `onSignUpClick shows error when passwords do not match`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password2")  // Different

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Passwords do not match", state.message)
    }

    @Test
    fun `onSignUpClick shows error when passwords differ in case`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("password1")  // Different case

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Passwords do not match", state.message)
    }

    // ========== Successful Signup Tests ==========

    @Test
    fun `onSignUpClick succeeds with valid inputs`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")
        coEvery { mockAccountService.createAccountWithEmail(any(), any()) } just Runs

        var navigationCalled = false
        viewModel.onSignUpClick { route, popUp ->
            navigationCalled = true
            assertEquals("intro", route)
            assertEquals("signup", popUp)
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(navigationCalled)
        assertTrue(viewModel.authState.value is AuthState.Success)
        coVerify { mockAccountService.createAccountWithEmail("test@example.com", "Password1") }
    }

    @Test
    fun `onSignUpClick transitions through Loading state`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")
        coEvery { mockAccountService.createAccountWithEmail(any(), any()) } just Runs

        val states = mutableListOf<AuthState>()

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.authState.collect { states.add(it) }
        }

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        job.cancel()

        assertTrue(states.contains(AuthState.Idle))
        assertTrue(states.contains(AuthState.Loading))
        assertTrue(states.any { it is AuthState.Success })
    }

    // ========== Signup Error Tests ==========

    @Test
    fun `onSignUpClick handles account already exists error`() = runTest {
        viewModel.updateEmail("existing@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")
        coEvery { mockAccountService.createAccountWithEmail(any(), any()) } throws Exception("Account already exists")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.isNotEmpty())
    }

    @Test
    fun `onSignUpClick handles network error`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")
        coEvery { mockAccountService.createAccountWithEmail(any(), any()) } throws Exception("Network error")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
    }

    @Test
    fun `onSignUpClick does not navigate on error`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")
        coEvery { mockAccountService.createAccountWithEmail(any(), any()) } throws Exception("Signup failed")

        var navigationCalled = false
        viewModel.onSignUpClick { _, _ ->
            navigationCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(!navigationCalled)
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    // ========== Validation Order Tests ==========

    @Test
    fun `onSignUpClick validates blank fields before email format`() = runTest {
        viewModel.updateEmail("")
        viewModel.updatePassword("Password1")
        viewModel.updateConfirmPassword("Password1")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Please fill all fields", state.message)
    }

    @Test
    fun `onSignUpClick validates email format before password strength`() = runTest {
        viewModel.updateEmail("invalid")
        viewModel.updatePassword("weak")
        viewModel.updateConfirmPassword("weak")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid email format", state.message)
    }

    @Test
    fun `onSignUpClick validates password strength before password match`() = runTest {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("weak")
        viewModel.updateConfirmPassword("different")

        viewModel.onSignUpClick { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("at least 6 characters") || state.message.contains("digit") || state.message.contains("lowercase") || state.message.contains("uppercase"))
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial email is empty`() {
        assertEquals("", viewModel.email.value)
    }

    @Test
    fun `initial password is empty`() {
        assertEquals("", viewModel.password.value)
    }

    @Test
    fun `initial confirmPassword is empty`() {
        assertEquals("", viewModel.confirmPassword.value)
    }

    @Test
    fun `initial authState is Idle`() {
        assertTrue(viewModel.authState.value is AuthState.Idle)
    }

    // ========== Google Signup Tests (Basic) ==========

    @Test
    fun `onSignUpWithGoogle handles error gracefully`() = runTest {
        val mockCredential = mockk<androidx.credentials.Credential>(relaxed = true)
        every { mockCredential.type } returns "unknown_type"

        viewModel.onSignUpWithGoogle(mockCredential) { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertTrue(state.message.contains("Unexpected credential"))
    }
}