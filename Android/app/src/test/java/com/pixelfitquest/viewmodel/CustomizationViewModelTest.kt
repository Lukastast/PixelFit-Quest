package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.CharacterData
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CustomizationViewModelTest {

    private lateinit var viewModel: CustomizationViewModel
    private lateinit var mockRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)

        // Default mock: return empty character data
        coEvery { mockRepository.getCharacterData() } returns flowOf(CharacterData())

        viewModel = CustomizationViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Initialization Tests ==========

    @Test
    fun `init loads character data from repository`() {
        val testData = CharacterData(
            gender = "female",
            variant = "basic",
            unlockedVariants = listOf("basic")
        )
        coEvery { mockRepository.getCharacterData() } returns flowOf(testData)

        val viewModel = CustomizationViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("female", viewModel.characterData.value.gender)
        assertEquals("basic", viewModel.characterData.value.variant)
        coVerify { mockRepository.getCharacterData() }
    }

    @Test
    fun `init uses default CharacterData when repository returns null`() {
        coEvery { mockRepository.getCharacterData() } returns flowOf(null)

        val viewModel = CustomizationViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("male", viewModel.characterData.value.gender)
        assertEquals("basic", viewModel.characterData.value.variant)
        assertEquals(listOf("basic"), viewModel.characterData.value.unlockedVariants)
    }

    // ========== Update Gender Tests ==========

    @Test
    fun `updateGender changes gender to male`() = runTest {
        viewModel.updateGender("male")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("male", viewModel.characterData.value.gender)
        coVerify { mockRepository.saveCharacterData(match { it.gender == "male" }) }
    }

    @Test
    fun `updateGender changes gender to female`() = runTest {
        viewModel.updateGender("female")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("female", viewModel.characterData.value.gender)
        coVerify { mockRepository.saveCharacterData(match { it.gender == "female" }) }
    }

    @Test
    fun `updateGender preserves other data fields`() = runTest {
        // Set up initial data
        coEvery { mockRepository.getCharacterData() } returns flowOf(
            CharacterData(
                gender = "male",
                variant = "male_fitness",
                unlockedVariants = listOf("basic", "male_fitness")
            )
        )
        val vm = CustomizationViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.updateGender("female")
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = vm.characterData.value
        assertEquals("female", saved.gender)
        assertEquals("male_fitness", saved.variant) // Preserved
        assertEquals(listOf("basic", "male_fitness"), saved.unlockedVariants) // Preserved
    }

    // ========== Update Variant Tests ==========

    @Test
    fun `updateVariant changes variant to fitness`() = runTest {
        viewModel.updateVariant("male_fitness")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("male_fitness", viewModel.characterData.value.variant)
        coVerify { mockRepository.saveCharacterData(match { it.variant == "male_fitness" }) }
    }

    @Test
    fun `updateVariant changes variant to basic`() = runTest {
        viewModel.updateVariant("basic")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("basic", viewModel.characterData.value.variant)
        coVerify { mockRepository.saveCharacterData(match { it.variant == "basic" }) }
    }

    @Test
    fun `updateVariant preserves gender and unlocked variants`() = runTest {
        coEvery { mockRepository.getCharacterData() } returns flowOf(
            CharacterData(
                gender = "female",
                variant = "basic",
                unlockedVariants = listOf("basic", "female_fitness")
            )
        )
        val vm = CustomizationViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.updateVariant("female_fitness")
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = vm.characterData.value
        assertEquals("female", saved.gender) // Preserved
        assertEquals("female_fitness", saved.variant)
        assertEquals(listOf("basic", "female_fitness"), saved.unlockedVariants) // Preserved
    }

    // ========== Buy Variant Tests ==========

    @Test
    fun `buyVariant purchases variant when user has enough coins`() = runTest {
        val gameData = UserGameData(coins = 150)
        coEvery { mockRepository.fetchUserGameDataOnce() } returns gameData
        coEvery { mockRepository.updateUserGameData(any()) } just Runs

        viewModel.buyVariant("male_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify coins were deducted
        coVerify {
            mockRepository.updateUserGameData(match {
                it["coins"] == 50
            })
        }

        // Verify variant was unlocked
        assertTrue(viewModel.characterData.value.unlockedVariants.contains("male_fitness"))

        // Verify variant was set as current
        assertEquals("male_fitness", viewModel.characterData.value.variant)

        // Verify data was saved
        coVerify { mockRepository.saveCharacterData(any()) }
    }

    @Test
    fun `buyVariant does nothing when user has insufficient coins`() = runTest {
        val gameData = UserGameData(coins = 50)
        coEvery { mockRepository.fetchUserGameDataOnce() } returns gameData

        viewModel.buyVariant("male_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify no coin update happened
        coVerify(exactly = 0) { mockRepository.updateUserGameData(any()) }

        // Verify variant was not unlocked
        assertFalse(viewModel.characterData.value.unlockedVariants.contains("male_fitness"))

        // Verify variant was not set
        assertEquals("basic", viewModel.characterData.value.variant)
    }

    @Test
    fun `buyVariant does nothing when variant is already unlocked`() = runTest {
        coEvery { mockRepository.getCharacterData() } returns flowOf(
            CharacterData(
                gender = "male",
                variant = "basic",
                unlockedVariants = listOf("basic", "male_fitness")
            )
        )
        val vm = CustomizationViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val gameData = UserGameData(coins = 150)
        coEvery { mockRepository.fetchUserGameDataOnce() } returns gameData

        vm.buyVariant("male_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify no coin update happened
        coVerify(exactly = 0) { mockRepository.updateUserGameData(any()) }

        // Unlocked variants should remain unchanged
        assertEquals(listOf("basic", "male_fitness"), vm.characterData.value.unlockedVariants)
    }

    @Test
    fun `buyVariant does nothing when fetchUserGameDataOnce returns null`() = runTest {
        coEvery { mockRepository.fetchUserGameDataOnce() } returns null

        viewModel.buyVariant("male_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify no coin update happened
        coVerify(exactly = 0) { mockRepository.updateUserGameData(any()) }

        // Verify variant was not unlocked
        assertFalse(viewModel.characterData.value.unlockedVariants.contains("male_fitness"))
    }

    @Test
    fun `buyVariant deducts exact price from coins`() = runTest {
        val gameData = UserGameData(coins = 100)
        coEvery { mockRepository.fetchUserGameDataOnce() } returns gameData
        coEvery { mockRepository.updateUserGameData(any()) } just Runs

        viewModel.buyVariant("male_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify exact deduction
        coVerify {
            mockRepository.updateUserGameData(match {
                it["coins"] == 0
            })
        }
    }

    @Test
    fun `buyVariant unlocks multiple variants independently`() = runTest {
        val gameData1 = UserGameData(coins = 200)
        val gameData2 = UserGameData(coins = 100)

        coEvery { mockRepository.fetchUserGameDataOnce() } returnsMany listOf(gameData1, gameData2)
        coEvery { mockRepository.updateUserGameData(any()) } just Runs

        // Buy first variant
        viewModel.buyVariant("male_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.characterData.value.unlockedVariants.contains("male_fitness"))

        // Buy second variant
        viewModel.buyVariant("female_fitness", 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Both should be unlocked
        assertTrue(viewModel.characterData.value.unlockedVariants.contains("male_fitness"))
        assertTrue(viewModel.characterData.value.unlockedVariants.contains("female_fitness"))
    }

    // ========== State Flow Tests ==========

    @Test
    fun `characterData flow emits updated values`() = runTest {
        val emissions = mutableListOf<CharacterData>()

        // Start collecting in the background
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.characterData.collect { emissions.add(it) }
        }

        // Allow initial emission to be collected
        testDispatcher.scheduler.advanceUntilIdle()

        // Make updates
        viewModel.updateGender("female")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateVariant("female_fitness")
        testDispatcher.scheduler.advanceUntilIdle()

        job.cancel()

        // Verify emissions
        assertTrue(emissions.size >= 3, "Expected at least 3 emissions but got ${emissions.size}")
        assertEquals("female", emissions.last().gender)
        assertEquals("female_fitness", emissions.last().variant)
    }

    // ========== Edge Cases ==========

    @Test
    fun `updateGender with empty string is saved`() = runTest {
        viewModel.updateGender("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.characterData.value.gender)
        coVerify { mockRepository.saveCharacterData(match { it.gender == "" }) }
    }

    @Test
    fun `updateVariant with empty string is saved`() = runTest {
        viewModel.updateVariant("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.characterData.value.variant)
        coVerify { mockRepository.saveCharacterData(match { it.variant == "" }) }
    }

    @Test
    fun `buyVariant with zero price still requires validation`() = runTest {
        val gameData = UserGameData(coins = 0)
        coEvery { mockRepository.fetchUserGameDataOnce() } returns gameData
        coEvery { mockRepository.updateUserGameData(any()) } just Runs

        viewModel.buyVariant("male_fitness", 0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should still work with 0 coins and 0 price
        assertTrue(viewModel.characterData.value.unlockedVariants.contains("male_fitness"))
    }
}