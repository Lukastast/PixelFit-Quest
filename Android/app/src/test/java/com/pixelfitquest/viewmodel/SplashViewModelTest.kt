package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.service.AccountService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private lateinit var viewModel: SplashViewModel
    private lateinit var mockAccountService: AccountService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockAccountService = mockk(relaxed = true)
        viewModel = SplashViewModel(mockAccountService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `checkAuthState returns true when user is authenticated`() = runTest {
        coEvery { mockAccountService.hasUser() } returns true

        var result: Boolean? = null
        viewModel.checkAuthState { result = it }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, result)
        coVerify { mockAccountService.hasUser() }
    }

    @Test
    fun `checkAuthState returns false when user is not authenticated`() = runTest {
        coEvery { mockAccountService.hasUser() } returns false

        var result: Boolean? = null
        viewModel.checkAuthState { result = it }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, result)
        coVerify { mockAccountService.hasUser() }
    }
}