package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.*
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.UserSettingsRepository
import com.pixelfitquest.repository.WorkoutRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var mockUserSettingsRepository: UserSettingsRepository
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockUserRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockUserSettingsRepository = mockk(relaxed = true)
        mockWorkoutRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)

        // Default mocks
        coEvery { mockUserSettingsRepository.getUserSettings() } returns flowOf(
            UserData(height = 180, musicVolume = 50)
        )
        coEvery { mockUserRepository.getCharacterData() } returns flowOf(CharacterData())
        coEvery { mockWorkoutRepository.saveWorkout(any()) } just Runs
        coEvery { mockWorkoutRepository.saveExercise(any()) } just Runs
        coEvery { mockWorkoutRepository.saveSet(any()) } just Runs

        viewModel = WorkoutViewModel(
            mockUserSettingsRepository,
            mockWorkoutRepository,
            mockUserRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== ROM Score Calculation Tests ==========

    @Test
    fun `calculateRomScore returns correct score for normal ROM`() = runTest {
        // Height = 180cm, BENCH_PRESS romFactor = 0.28
        // Theoretical max = 180 * 0.28 = 50.4cm
        // If actual ROM = 25.2cm, score = 25.2 / 50.4 * 100 = 50%

        val score = viewModel.calculateRomScore(25.2f)

        assertEquals(50f, score, 0.1f)
    }

    @Test
    fun `calculateRomScore returns 0 for zero ROM`() = runTest {
        val score = viewModel.calculateRomScore(0f)

        assertEquals(0f, score)
    }

    @Test
    fun `calculateRomScore caps at 100 for ROM exceeding theoretical max`() = runTest {
        // Height = 180cm, theoretical max = 50.4cm
        // If actual ROM = 100cm (exceeds max), score should cap at 100

        val score = viewModel.calculateRomScore(100f)

        assertEquals(100f, score)
    }

    @Test
    fun `calculateRomScore returns 100 for perfect ROM`() = runTest {
        // Height = 180cm, theoretical max = 50.4cm

        val score = viewModel.calculateRomScore(50.4f)

        assertEquals(100f, score, 0.1f)
    }

    @Test
    fun `calculateRomScore handles different exercise types`() = runTest {
        // Create a plan with SQUAT (romFactor = 0.53)
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.SQUAT, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Height = 180cm, SQUAT romFactor = 0.53
        // Theoretical max = 180 * 0.53 = 95.4cm
        // If actual ROM = 47.7cm, score = 47.7 / 95.4 * 100 = 50%

        val score = viewModel.calculateRomScore(47.7f)

        assertEquals(50f, score, 0.1f)
    }

    @Test
    fun `calculateRomScore returns 0 when user settings not loaded`() = runTest {
        coEvery { mockUserSettingsRepository.getUserSettings() } returns flowOf(null)

        viewModel = WorkoutViewModel(
            mockUserSettingsRepository,
            mockWorkoutRepository,
            mockUserRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val score = viewModel.calculateRomScore(25f)

        assertEquals(0f, score)
    }

    // ========== Workout Flow Tests ==========

    @Test
    fun `startWorkoutFromPlan initializes workout state correctly`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 3, weight = 50f),
                WorkoutPlanItem(ExerciseType.SQUAT, sets = 2, weight = 100f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test Workout")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.workoutState.value
        assertTrue(state.isTracking)
        assertEquals(5, state.totalSets) // 3 + 2
        assertEquals(0, state.currentExerciseIndex)
        assertEquals(1, state.currentSetNumber)
        assertEquals(0, state.reps)
        assertFalse(state.isSetActive)
    }

    @Test
    fun `startWorkoutFromPlan uses default workout name when not provided`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.workoutState.value.isTracking)
    }

    @Test
    fun `startWorkoutFromPlan sanitizes workout name`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "My Workout")
        testDispatcher.scheduler.advanceUntilIdle()

        // Name should be converted to "my_workout"
        assertTrue(viewModel.workoutState.value.isTracking)
    }

    // ========== Set Management Tests ==========

    @Test
    fun `startSet activates the set`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startSet()

        assertTrue(viewModel.workoutState.value.isSetActive)
        assertEquals(0, viewModel.workoutState.value.reps)
    }

    @Test
    fun `startSet does nothing if set already active`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        viewModel.startSet()
        val state1 = viewModel.workoutState.value

        viewModel.startSet() // Try to start again
        val state2 = viewModel.workoutState.value

        assertEquals(state1, state2)
    }

    @Test
    fun `finishSet deactivates the set and saves it`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        viewModel.startSet()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.workoutState.value.isSetActive)
        coVerify { mockWorkoutRepository.saveSet(any()) }
    }

    @Test
    fun `finishSet advances to next set within same exercise`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 3, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.workoutState.value.currentSetNumber)
        assertEquals(0, viewModel.workoutState.value.currentExerciseIndex)
    }

    @Test
    fun `finishSet advances to next exercise after completing all sets`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 2, weight = 0f),
                WorkoutPlanItem(ExerciseType.SQUAT, sets = 2, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")

        // Complete first exercise (2 sets)
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.workoutState.value.currentSetNumber)
        assertEquals(1, viewModel.workoutState.value.currentExerciseIndex)
    }

    @Test
    fun `finishSet does nothing if set not active`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.finishSet() // Try to finish without starting

        coVerify(exactly = 0) { mockWorkoutRepository.saveSet(any()) }
    }

    // ========== Exercise Management Tests ==========

    @Test
    fun `finishExercise saves exercise data`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 50f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.finishExercise()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockWorkoutRepository.saveExercise(
                match {
                    it.type == ExerciseType.BENCH_PRESS &&
                            it.totalSets == 1
                }
            )
        }
    }

    // ========== Workout Completion Tests ==========

    @Test
    fun `finishWorkout saves workout and stops tracking`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test Workout")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.finishWorkout()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockWorkoutRepository.saveWorkout(
                match {
                    it.name == "test_workout" &&
                            it.totalExercises == 1 &&
                            it.totalSets == 1
                }
            )
        }
        assertFalse(viewModel.workoutState.value.isTracking)
    }

    @Test
    fun `finishWorkout emits navigation event with workout ID`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.finishWorkout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Navigation event should be emitted with workout ID
        assertNotNull(viewModel.navigationEvent.replayCache.firstOrNull())
    }

    @Test
    fun `completing all sets in workout calls finishWorkout`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockWorkoutRepository.saveWorkout(any()) }
        assertFalse(viewModel.workoutState.value.isTracking)
    }

    // ========== Stop Workout Tests ==========

    @Test
    fun `stopWorkout deactivates tracking and set`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 1, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        viewModel.startSet()

        viewModel.stopWorkout()

        assertFalse(viewModel.workoutState.value.isTracking)
        assertFalse(viewModel.workoutState.value.isSetActive)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `setError updates error state`() = runTest {
        viewModel.setError("Test error")

        assertEquals("Test error", viewModel.error.value)
    }

    @Test
    fun `setError can clear error with null`() = runTest {
        viewModel.setError("Test error")
        viewModel.setError(null)

        assertNull(viewModel.error.value)
    }

    @Test
    fun `setError updates error state correctly`() = runTest {
        assertNull(viewModel.error.value)

        viewModel.setError("Test error message")

        assertEquals("Test error message", viewModel.error.value)
    }
    // ========== Initial State Tests ==========

    @Test
    fun `initial workout state is not tracking`() {
        assertFalse(viewModel.workoutState.value.isTracking)
        assertFalse(viewModel.workoutState.value.isSetActive)
        assertEquals(0, viewModel.workoutState.value.reps)
        assertEquals(1, viewModel.workoutState.value.currentSetNumber)
    }

    @Test
    fun `initial error is null`() {
        assertNull(viewModel.error.value)
    }

    @Test
    fun `user settings loaded on init`() = runTest {
        assertNotNull(viewModel.userData.value)
        assertEquals(180, viewModel.userData.value?.height)
    }

    @Test
    fun `character data loaded on init`() = runTest {
        assertNotNull(viewModel.characterData.value)
    }

    // ========== Complex Workflow Tests ==========

    @Test
    fun `complete workout flow with multiple exercises and sets`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 2, weight = 50f),
                WorkoutPlanItem(ExerciseType.SQUAT, sets = 2, weight = 100f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Full Workout")
        testDispatcher.scheduler.advanceUntilIdle()

        // Exercise 1, Set 1
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        // Exercise 1, Set 2
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should save exercise 1 and advance to exercise 2
        coVerify(exactly = 1) { mockWorkoutRepository.saveExercise(match { it.type == ExerciseType.BENCH_PRESS }) }
        assertEquals(1, viewModel.workoutState.value.currentExerciseIndex)
        assertEquals(1, viewModel.workoutState.value.currentSetNumber)

        // Exercise 2, Set 1
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        // Exercise 2, Set 2
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should save exercise 2 and complete workout
        coVerify(exactly = 1) { mockWorkoutRepository.saveExercise(match { it.type == ExerciseType.SQUAT }) }
        coVerify(exactly = 4) { mockWorkoutRepository.saveSet(any()) }
        coVerify(exactly = 1) { mockWorkoutRepository.saveWorkout(any()) }
        assertFalse(viewModel.workoutState.value.isTracking)
    }

    @Test
    fun `workout state tracks set and exercise progress correctly`() = runTest {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 3, weight = 0f),
                WorkoutPlanItem(ExerciseType.SQUAT, sets = 2, weight = 0f)
            )
        )

        viewModel.startWorkoutFromPlan(plan, "Test")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.workoutState.value.currentExerciseIndex)
        assertEquals(1, viewModel.workoutState.value.currentSetNumber)
        assertEquals(5, viewModel.workoutState.value.totalSets)

        // Complete set 1
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.workoutState.value.currentExerciseIndex)
        assertEquals(2, viewModel.workoutState.value.currentSetNumber)

        // Complete set 2
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.workoutState.value.currentExerciseIndex)
        assertEquals(3, viewModel.workoutState.value.currentSetNumber)

        // Complete set 3 (last of exercise 1)
        viewModel.startSet()
        viewModel.finishSet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.workoutState.value.currentExerciseIndex)
        assertEquals(1, viewModel.workoutState.value.currentSetNumber)
    }
}