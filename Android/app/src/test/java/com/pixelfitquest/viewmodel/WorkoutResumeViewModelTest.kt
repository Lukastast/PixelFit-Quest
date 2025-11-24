package com.pixelfitquest.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.pixelfitquest.model.*
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.WorkoutRepository
import com.pixelfitquest.ui.view.ExerciseWithSets
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
class WorkoutResumeViewModelTest {

    private lateinit var viewModel: WorkoutResumeViewModel
    private lateinit var mockSavedStateHandle: SavedStateHandle
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockUserRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testWorkoutId = "workout-123"
    private val testWorkout = Workout(
        id = testWorkoutId,
        date = "2024-01-01",
        name = "Test Workout",
        totalExercises = 2,
        totalSets = 4,
        overallScore = 85f,
        rewardsAwarded = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockSavedStateHandle = mockk(relaxed = true)
        mockWorkoutRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)

        // Default mocks
        every { mockSavedStateHandle.get<String>("workoutId") } returns testWorkoutId
        coEvery { mockUserRepository.getUserGameData() } returns flowOf(
            UserGameData(level = 1, exp = 0, coins = 100)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Initialization Tests ==========

    @Test
    fun `init loads workout data when workoutId is present`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 3, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 90f
        )

        coEvery { mockWorkoutRepository.getWorkout(testWorkoutId) } returns testWorkout
        coEvery { mockWorkoutRepository.getExercisesByWorkoutId(testWorkoutId) } returns listOf(testExercise)
        coEvery { mockWorkoutRepository.getSetsByWorkoutId(testWorkoutId) } returns listOf(testSet)
        coEvery { mockWorkoutRepository.updateWorkout(any(), any()) } just Runs
        coEvery { mockUserRepository.updateExp(any()) } just Runs
        coEvery { mockUserRepository.updateUserGameData(any()) } just Runs

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockWorkoutRepository.getWorkout(testWorkoutId) }
        assertEquals(1, viewModel.exercisesWithSets.value.size)
    }

    @Test
    fun `init does not load data when workoutId is blank`() = runTest {
        every { mockSavedStateHandle.get<String>("workoutId") } returns ""

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockWorkoutRepository.getWorkout(any()) }
    }

    // ========== XP Multiplier Tests ==========

    @Test
    fun `calculateAndAwardRewards applies 2x multiplier for perfect score 90+`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 95f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 10 reps * 2.0 multiplier = 20 XP
        assertEquals(20, viewModel.summary.value.totalXp)
    }

    @Test
    fun `calculateAndAwardRewards applies 1_5x multiplier for great score 80-89`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 85f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 10 reps * 1.5 multiplier = 15 XP
        assertEquals(15, viewModel.summary.value.totalXp)
    }

    @Test
    fun `calculateAndAwardRewards applies 1x multiplier for score below 80`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 70f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 10 reps * 1.0 multiplier = 10 XP
        assertEquals(10, viewModel.summary.value.totalXp)
    }

    @Test
    fun `calculateAndAwardRewards uses exactly 90 as perfect threshold`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 90f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 10 reps * 2.0 multiplier = 20 XP
        assertEquals(20, viewModel.summary.value.totalXp)
    }

    @Test
    fun `calculateAndAwardRewards uses exactly 80 as great threshold`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 80f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 10 reps * 1.5 multiplier = 15 XP
        assertEquals(15, viewModel.summary.value.totalXp)
    }

    // ========== Coin Calculation Tests ==========

    @Test
    fun `calculateAndAwardRewards calculates coins as reps divided by 10`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 50,
            workoutScore = 85f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 50 reps / 10 = 5 coins
        assertEquals(5, viewModel.summary.value.totalCoins)
    }

    @Test
    fun `calculateAndAwardRewards rounds down coins for fractional division`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 19,
            workoutScore = 85f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 19 reps / 10 = 1 coin (rounds down)
        assertEquals(1, viewModel.summary.value.totalCoins)
    }

    @Test
    fun `calculateAndAwardRewards awards zero coins for less than 10 reps`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 9,
            workoutScore = 85f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // 9 reps / 10 = 0 coins
        assertEquals(0, viewModel.summary.value.totalCoins)
    }

    // ========== Multiple Sets/Exercises Tests ==========

    @Test
    fun `calculateAndAwardRewards sums XP from multiple sets`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 2, 100f)
        val sets = listOf(
            WorkoutSet(
                id = "set-1",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 1,
                reps = 10,
                workoutScore = 90f
            ),
            WorkoutSet(
                id = "set-2",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 2,
                reps = 10,
                workoutScore = 85f
            )
        )

        setupWorkoutWithSets(listOf(testExercise), sets)

        // 20 + 15 = 35 XP
        assertEquals(35, viewModel.summary.value.totalXp)
    }

    @Test
    fun `calculateAndAwardRewards sums coins from multiple sets`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 2, 100f)
        val sets = listOf(
            WorkoutSet(
                id = "set-1",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 1,
                reps = 20,
                workoutScore = 90f
            ),
            WorkoutSet(
                id = "set-2",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 2,
                reps = 30,
                workoutScore = 85f
            )
        )

        setupWorkoutWithSets(listOf(testExercise), sets)

        // (20 + 30) / 10 = 5 coins
        assertEquals(5, viewModel.summary.value.totalCoins)
    }

    @Test
    fun `calculateAndAwardRewards handles multiple exercises`() = runTest {
        val exercises = listOf(
            Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f),
            Exercise("ex-2", testWorkoutId, ExerciseType.SQUAT, 1, 100f)
        )
        val sets = listOf(
            WorkoutSet(
                id = "set-1",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 1,
                reps = 10,
                workoutScore = 90f
            ),
            WorkoutSet(
                id = "set-2",
                exerciseId = "ex-2",
                workoutId = testWorkoutId,
                setNumber = 1,
                reps = 10,
                workoutScore = 85f
            )
        )

        setupWorkoutWithSets(exercises, sets)

        // 20 + 15 = 35 XP
        assertEquals(35, viewModel.summary.value.totalXp)
        assertEquals(2, viewModel.exercisesWithSets.value.size)
    }

    // ========== One-Time Reward Tests ==========

    @Test
    fun `rewards are not awarded when rewardsAwarded is true`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 90f
        )

        val awardedWorkout = testWorkout.copy(rewardsAwarded = true)
        coEvery { mockWorkoutRepository.getWorkout(testWorkoutId) } returns awardedWorkout
        coEvery { mockWorkoutRepository.getExercisesByWorkoutId(testWorkoutId) } returns listOf(testExercise)
        coEvery { mockWorkoutRepository.getSetsByWorkoutId(testWorkoutId) } returns listOf(testSet)

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Rewards should NOT be calculated (summary stays at 0)
        assertEquals(0, viewModel.summary.value.totalXp)
        assertEquals(0, viewModel.summary.value.totalCoins)
        coVerify(exactly = 0) { mockUserRepository.updateExp(any()) }
        coVerify(exactly = 0) { mockUserRepository.updateUserGameData(any()) }
    }

    @Test
    fun `rewards are awarded and flag is set when rewardsAwarded is false`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 90f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        coVerify { mockUserRepository.updateExp(20) }
        coVerify { mockUserRepository.updateUserGameData(any()) }
        coVerify { mockWorkoutRepository.updateWorkout(testWorkoutId, match { it["rewardsAwarded"] == true }) }
    }

    // ========== Edge Cases Tests ==========

    @Test
    fun `calculateAndAwardRewards handles negative reps as zero`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = -5,
            workoutScore = 90f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // Negative reps coerced to 0
        assertEquals(0, viewModel.summary.value.totalXp)
        assertEquals(0, viewModel.summary.value.totalCoins)
    }

    @Test
    fun `calculateAndAwardRewards coerces score above 100 to 100`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = 150f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // Should still apply 2x multiplier (score >= 90)
        assertEquals(20, viewModel.summary.value.totalXp)
    }

    @Test
    fun `calculateAndAwardRewards coerces score below 0 to 0`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)
        val testSet = WorkoutSet(
            id = "set-1",
            exerciseId = "ex-1",
            workoutId = testWorkoutId,
            setNumber = 1,
            reps = 10,
            workoutScore = -10f
        )

        setupWorkoutWithSets(listOf(testExercise), listOf(testSet))

        // Should apply 1x multiplier (score < 80)
        assertEquals(10, viewModel.summary.value.totalXp)
    }

    @Test
    fun `loadWorkoutData handles empty exercises`() = runTest {
        coEvery { mockWorkoutRepository.getWorkout(testWorkoutId) } returns testWorkout
        coEvery { mockWorkoutRepository.getExercisesByWorkoutId(testWorkoutId) } returns emptyList()
        coEvery { mockWorkoutRepository.getSetsByWorkoutId(testWorkoutId) } returns emptyList()

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.exercisesWithSets.value.isEmpty())
        assertEquals(0, viewModel.summary.value.totalXp)
    }

    @Test
    fun `loadWorkoutData handles exercises with no sets`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 1, 100f)

        coEvery { mockWorkoutRepository.getWorkout(testWorkoutId) } returns testWorkout
        coEvery { mockWorkoutRepository.getExercisesByWorkoutId(testWorkoutId) } returns listOf(testExercise)
        coEvery { mockWorkoutRepository.getSetsByWorkoutId(testWorkoutId) } returns emptyList()

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Exercise with no sets should be filtered out
        assertTrue(viewModel.exercisesWithSets.value.isEmpty())
    }

    @Test
    fun `loadWorkoutData handles missing workout gracefully`() = runTest {
        coEvery { mockWorkoutRepository.getWorkout(testWorkoutId) } returns null

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.exercisesWithSets.value.isEmpty())
        coVerify(exactly = 0) { mockWorkoutRepository.getExercisesByWorkoutId(any()) }
    }

    // ========== Average Score Tests ==========

    @Test
    fun `calculateAndAwardRewards calculates average score correctly`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 2, 100f)
        val sets = listOf(
            WorkoutSet(
                id = "set-1",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 1,
                reps = 10,
                workoutScore = 80f
            ),
            WorkoutSet(
                id = "set-2",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 2,
                reps = 10,
                workoutScore = 90f
            )
        )

        setupWorkoutWithSets(listOf(testExercise), sets)

        // Average: (80 + 90) / 2 = 85
        assertEquals(85f, viewModel.summary.value.avgScore)
    }

    @Test
    fun `exercisesWithSets includes per-exercise average score`() = runTest {
        val testExercise = Exercise("ex-1", testWorkoutId, ExerciseType.BENCH_PRESS, 2, 100f)
        val sets = listOf(
            WorkoutSet(
                id = "set-1",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 1,
                reps = 10,
                workoutScore = 70f
            ),
            WorkoutSet(
                id = "set-2",
                exerciseId = "ex-1",
                workoutId = testWorkoutId,
                setNumber = 2,
                reps = 10,
                workoutScore = 90f
            )
        )

        setupWorkoutWithSets(listOf(testExercise), sets)

        val exerciseWithSets = viewModel.exercisesWithSets.value.first()
        // Average: (70 + 90) / 2 = 80
        assertEquals(80, exerciseWithSets.avgWorkoutScore)
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial summary is zero`() {
        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)

        assertEquals(0, viewModel.summary.value.totalXp)
        assertEquals(0, viewModel.summary.value.totalCoins)
        assertEquals(0f, viewModel.summary.value.avgScore)
    }

    @Test
    fun `initial error is null`() {
        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)

        assertNull(viewModel.error.value)
    }

    @Test
    fun `initial exercisesWithSets is empty`() {
        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)

        assertTrue(viewModel.exercisesWithSets.value.isEmpty())
    }

    // ========== Helper Function ==========

    private suspend fun setupWorkoutWithSets(exercises: List<Exercise>, sets: List<WorkoutSet>) {
        coEvery { mockWorkoutRepository.getWorkout(testWorkoutId) } returns testWorkout
        coEvery { mockWorkoutRepository.getExercisesByWorkoutId(testWorkoutId) } returns exercises
        coEvery { mockWorkoutRepository.getSetsByWorkoutId(testWorkoutId) } returns sets
        coEvery { mockWorkoutRepository.updateWorkout(any(), any()) } just Runs
        coEvery { mockUserRepository.updateExp(any()) } just Runs
        coEvery { mockUserRepository.updateUserGameData(any()) } just Runs

        viewModel = WorkoutResumeViewModel(mockSavedStateHandle, mockWorkoutRepository, mockUserRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}