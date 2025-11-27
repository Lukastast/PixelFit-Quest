package com.pixelfitquest.viewmodel

import android.content.Context
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.Workout
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.WorkoutRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var mockAccountService: AccountService
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockContext: Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockAccountService = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)
        mockWorkoutRepository = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        // Default mocks
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(
            UserGameData(level = 1, exp = 0, coins = 0, streak = 0)
        )
        coEvery { mockUserRepository.loadProgressionConfig() } just Runs
        coEvery { mockUserRepository.getExpRequiredForLevel(any()) } returns 100
        coEvery { mockUserRepository.getMaxLevel() } returns 100
        coEvery { mockWorkoutRepository.getAllCompletedWorkouts() } returns emptyList()
        coEvery { mockUserRepository.getLeaderboard() } returns emptyList()

        viewModel = HomeViewModel(
            mockAccountService,
            mockUserRepository,
            mockWorkoutRepository,
            mockContext
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Coin Management Tests ==========

    @Test
    fun `addCoins increases coins by specified amount`() = runTest {
        val initialData = UserGameData(coins = 100)
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(initialData)
        coEvery { mockUserRepository.updateUserGameData(any()) } just Runs

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        // Call initialize to populate userGameData state
        viewModel.initialize({}, null)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addCoins(50)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockUserRepository.updateUserGameData(match {
                it["coins"] == 150
            })
        }
    }

    @Test
    fun `addCoins does nothing when amount is zero`() = runTest {
        viewModel.addCoins(0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockUserRepository.updateUserGameData(any()) }
    }

    @Test
    fun `addCoins does nothing when amount is negative`() = runTest {
        viewModel.addCoins(-50)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockUserRepository.updateUserGameData(any()) }
    }

    @Test
    fun `addCoins sets error when repository throws exception`() = runTest {
        val initialData = UserGameData(coins = 100)
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(initialData)
        coEvery { mockUserRepository.updateUserGameData(any()) } throws Exception("Database error")

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        // Initialize to populate state
        viewModel.initialize({}, null)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addCoins(50)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Database error"))
    }

    // ========== EXP Management Tests ==========

    @Test
    fun `addExp calls repository with correct amount`() = runTest {
        coEvery { mockUserRepository.updateExp(any()) } just Runs

        viewModel.addExp(100)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUserRepository.updateExp(100) }
    }

    @Test
    fun `addExp does nothing when amount is zero`() = runTest {
        viewModel.addExp(0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockUserRepository.updateExp(any()) }
    }

    @Test
    fun `addExp does nothing when amount is negative`() = runTest {
        viewModel.addExp(-50)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockUserRepository.updateExp(any()) }
    }

    @Test
    fun `addExp sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.updateExp(any()) } throws Exception("Update failed")

        viewModel.addExp(50)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Update failed"))
    }

    // ========== Streak Management Tests ==========

    @Test
    fun `incrementStreak calls repository with increment true`() = runTest {
        coEvery { mockUserRepository.updateStreak(any(), any()) } just Runs

        viewModel.incrementStreak()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUserRepository.updateStreak(increment = true) }
    }

    @Test
    fun `resetStreak calls repository with reset true`() = runTest {
        coEvery { mockUserRepository.updateStreak(any(), any()) } just Runs

        viewModel.resetStreak()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUserRepository.updateStreak(reset = true) }
    }

    @Test
    fun `incrementStreak sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.updateStreak(any(), any()) } throws Exception("Network error")

        viewModel.incrementStreak()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Network error"))
    }

    @Test
    fun `resetStreak sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.updateStreak(any(), any()) } throws Exception("Network error")

        viewModel.resetStreak()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Network error"))
    }

    // ========== User Data Loading Tests ==========

    @Test
    fun `userGameData flow emits loaded data`() = runTest {
        val testData = UserGameData(level = 5, exp = 250, coins = 500, streak = 3)
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(testData)

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        // Call initialize to trigger loadUserData
        viewModel.initialize({}, null)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.userGameData.value
        assertNotNull(result)
        assertEquals(5, result.level)
        assertEquals(250, result.exp)
        assertEquals(500, result.coins)
        assertEquals(3, result.streak)
    }

    @Test
    fun `currentMaxExp is calculated for next level`() = runTest {
        val testData = UserGameData(level = 5, exp = 250)
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(testData)
        coEvery { mockUserRepository.getExpRequiredForLevel(6) } returns 600
        coEvery { mockUserRepository.getMaxLevel() } returns 100

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        viewModel.initialize({}, null)  // Add this
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(600, viewModel.currentMaxExp.value)
        coVerify { mockUserRepository.getExpRequiredForLevel(6) }
    }

    @Test
    fun `currentMaxExp uses max level exp when at max level`() = runTest {
        val testData = UserGameData(level = 100, exp = 9999)
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(testData)
        coEvery { mockUserRepository.getMaxLevel() } returns 100
        coEvery { mockUserRepository.getExpRequiredForLevel(100) } returns 10000

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        viewModel.initialize({}, null)  // Add this
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(10000, viewModel.currentMaxExp.value)
        coVerify { mockUserRepository.getExpRequiredForLevel(100) }
    }

    @Test
    fun `loadUserData sets error when repository throws exception`() = runTest {
        coEvery { mockUserRepository.getUserGameData() } throws Exception("Network error")
        // Also need to mock these since initialize() calls them
        coEvery { mockUserRepository.loadProgressionConfig() } just Runs
        coEvery { mockWorkoutRepository.getAllCompletedWorkouts() } returns emptyList()
        coEvery { mockUserRepository.getLeaderboard() } returns emptyList()

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        // Call initialize to trigger loadUserData
        viewModel.initialize({}, null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Network error"))
    }

    // ========== Workout Tests ==========

    @Test
    fun `workouts flow emits sorted completed workouts`() = runTest {
        val workouts = listOf(
            Workout("1", "2024-01-10", "Chest Day", 3, 12, 85f),
            Workout("2", "2024-01-15", "Leg Day", 4, 16, 90f),
            Workout("3", "2024-01-12", "Back Day", 3, 10, 88f)
        )
        coEvery { mockWorkoutRepository.getAllCompletedWorkouts() } returns workouts

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        // Call initialize to trigger fetchCompletedWorkouts
        viewModel.initialize({}, null)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.workouts.value
        assertEquals(3, result.size)
        assertEquals("2024-01-15", result[0].date) // Latest first
        assertEquals("2024-01-12", result[1].date)
        assertEquals("2024-01-10", result[2].date)
    }

    // ========== Achievement Tests ==========

    @Test
    fun `achievements are unlocked based on workout count`() = runTest {
        val workouts = List(15) { index ->
            Workout("$index", "2024-01-$index", "Workout $index", 3, 12, 85f)
        }
        coEvery { mockWorkoutRepository.getAllCompletedWorkouts() } returns workouts

        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)

        // Call initialize to trigger fetchCompletedWorkouts which sets achievements
        viewModel.initialize({}, null)
        testDispatcher.scheduler.advanceUntilIdle()

        val achievements = viewModel.achievements.value

        assertTrue(achievements.isNotEmpty())

        val firstAchievement = achievements.find { it.first.requiredWorkouts == 1 }
        assertNotNull(firstAchievement)
        assertTrue(firstAchievement.second)

        val secondAchievement = achievements.find { it.first.requiredWorkouts == 10 }
        assertNotNull(secondAchievement)
        assertTrue(secondAchievement.second)

        val thirdAchievement = achievements.find { it.first.requiredWorkouts == 50 }
        assertNotNull(thirdAchievement)
        assertFalse(thirdAchievement.second)
    }

    // ========== Daily Mission Tests ==========

    @Test
    fun `daily missions are generated consistently with same date`() = runTest {
        val vm1 = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)
        vm1.initialize({}, null)  // Add this
        testDispatcher.scheduler.advanceUntilIdle()

        val missions1 = vm1.dailyMissions.value

        val vm2 = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)
        vm2.initialize({}, null)  // Add this
        testDispatcher.scheduler.advanceUntilIdle()

        val missions2 = vm2.dailyMissions.value

        assertEquals(3, missions1.size)
        assertEquals(3, missions2.size)
        assertEquals(missions1, missions2)
    }

    @Test
    fun `daily missions pairs missions with rewards`() = runTest {
        viewModel = HomeViewModel(mockAccountService, mockUserRepository, mockWorkoutRepository, mockContext)
        viewModel.initialize({}, null)  // Add this
        testDispatcher.scheduler.advanceUntilIdle()

        val missions = viewModel.dailyMissions.value

        assertEquals(3, missions.size)
        missions.forEach { (mission, reward) ->
            assertTrue(mission.isNotEmpty())
            assertTrue(reward.contains(":"))
        }
    }

    // ========== State Flow Tests ==========

    @Test
    fun `error flow starts as null`() {
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun `todaySteps flow starts at zero`() {
        assertEquals(0L, viewModel.todaySteps.value)
    }

    @Test
    fun `stepGoal flow starts at zero`() {
        assertEquals(0, viewModel.stepGoal.value)
    }

    @Test
    fun `rank flow starts at zero`() {
        assertEquals(0, viewModel.rank.value)
    }

    @Test
    fun `totalUsers flow starts at zero`() {
        assertEquals(0, viewModel.totalUsers.value)
    }

    @Test
    fun `completedMissions flow starts empty`() {
        assertTrue(viewModel.completedMissions.value.isEmpty())
    }
}