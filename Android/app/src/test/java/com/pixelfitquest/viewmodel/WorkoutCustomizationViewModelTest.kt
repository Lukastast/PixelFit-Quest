package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.workout.WorkoutPlan
import com.pixelfitquest.model.workout.WorkoutPlanItem
import com.pixelfitquest.model.workout.WorkoutTemplate
import com.pixelfitquest.model.workout.ExerciseType
import com.pixelfitquest.repository.WorkoutTemplateRepository
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
class WorkoutCustomizationViewModelTest {

    private lateinit var viewModel: WorkoutCustomizationViewModel
    private lateinit var mockTemplateRepository: WorkoutTemplateRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockTemplateRepository = mockk(relaxed = true)

        coEvery { mockTemplateRepository.getTemplates() } returns flowOf(emptyList())

        viewModel = WorkoutCustomizationViewModel(mockTemplateRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== Initialization Tests ==========

    @Test
    fun `init loads templates from repository`() = runTest {
        val templates = listOf(
            WorkoutTemplate("1", "Template 1", WorkoutPlan(emptyList())),
            WorkoutTemplate("2", "Template 2", WorkoutPlan(emptyList()))
        )
        coEvery { mockTemplateRepository.getTemplates() } returns flowOf(templates)

        viewModel = WorkoutCustomizationViewModel(mockTemplateRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.templates.value.size)
        assertEquals("Template 1", viewModel.templates.value[0].name)
        assertEquals("Template 2", viewModel.templates.value[1].name)
    }

    // ========== Exercise Toggle Tests ==========

    @Test
    fun `toggleExercise adds exercise when not present`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)

        val selections = viewModel.uiState.value.selections
        assertTrue(selections.containsKey(ExerciseType.BICEP_CURL))
        assertEquals(3, selections[ExerciseType.BICEP_CURL]?.sets)
        assertEquals(0f, selections[ExerciseType.BICEP_CURL]?.weight)
    }

    @Test
    fun `toggleExercise removes exercise when already present`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        assertTrue(viewModel.uiState.value.selections.containsKey(ExerciseType.BICEP_CURL))

        viewModel.toggleExercise(ExerciseType.BICEP_CURL)
        assertFalse(viewModel.uiState.value.selections.containsKey(ExerciseType.BICEP_CURL))
    }

    @Test
    fun `toggleExercise coerces sets to valid range`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 15, weight = 0f)

        val selections = viewModel.uiState.value.selections
        assertEquals(10, selections[ExerciseType.BICEP_CURL]?.sets)
    }

    @Test
    fun `toggleExercise coerces weight to valid range`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 3, weight = 600f)

        val selections = viewModel.uiState.value.selections
        assertEquals(500f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    @Test
    fun `toggleExercise handles negative sets`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = -5, weight = 0f)

        val selections = viewModel.uiState.value.selections
        assertEquals(1, selections[ExerciseType.BICEP_CURL]?.sets)
    }

    @Test
    fun `toggleExercise handles negative weight`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 3, weight = -50f)

        val selections = viewModel.uiState.value.selections
        assertEquals(0f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    // ========== Update Sets Tests ==========

    @Test
    fun `updateSets updates sets for existing exercise`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.updateSets(ExerciseType.BICEP_CURL, 5)

        val selections = viewModel.uiState.value.selections
        assertEquals(5, selections[ExerciseType.BICEP_CURL]?.sets)
    }

    @Test
    fun `updateSets does nothing for non-existent exercise`() {
        val initialSelections = viewModel.uiState.value.selections
        viewModel.updateSets(ExerciseType.BICEP_CURL, 5)

        assertEquals(initialSelections, viewModel.uiState.value.selections)
    }

    @Test
    fun `updateSets ignores negative values`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.updateSets(ExerciseType.BICEP_CURL, -5)

        val selections = viewModel.uiState.value.selections
        assertEquals(3, selections[ExerciseType.BICEP_CURL]?.sets)
    }

    @Test
    fun `updateSets ignores zero`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.updateSets(ExerciseType.BICEP_CURL, 0)

        val selections = viewModel.uiState.value.selections
        assertEquals(3, selections[ExerciseType.BICEP_CURL]?.sets)
    }

    @Test
    fun `updateSets coerces to maximum of 10`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.updateSets(ExerciseType.BICEP_CURL, 15)

        val selections = viewModel.uiState.value.selections
        assertEquals(10, selections[ExerciseType.BICEP_CURL]?.sets)
    }

    @Test
    fun `updateSets preserves weight`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 3, weight = 100f)
        viewModel.updateSets(ExerciseType.BENCH_PRESS, 5)

        val selections = viewModel.uiState.value.selections
        assertEquals(5, selections[ExerciseType.BENCH_PRESS]?.sets)
        assertEquals(100f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    // ========== Update Weight Tests ==========

    @Test
    fun `updateWeight updates weight for existing exercise`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 3, weight = 100f)
        viewModel.updateWeight(ExerciseType.BENCH_PRESS, 150f)

        val selections = viewModel.uiState.value.selections
        assertEquals(150f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    @Test
    fun `updateWeight does nothing for non-existent exercise`() {
        val initialSelections = viewModel.uiState.value.selections
        viewModel.updateWeight(ExerciseType.BENCH_PRESS, 100f)

        assertEquals(initialSelections, viewModel.uiState.value.selections)
    }

    @Test
    fun `updateWeight ignores negative values`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 3, weight = 100f)
        viewModel.updateWeight(ExerciseType.BENCH_PRESS, -50f)

        val selections = viewModel.uiState.value.selections
        assertEquals(100f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    @Test
    fun `updateWeight allows zero weight`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 50f)
        viewModel.updateWeight(ExerciseType.BICEP_CURL, 0f)

        val selections = viewModel.uiState.value.selections
        assertEquals(0f, selections[ExerciseType.BICEP_CURL]?.weight)
    }

    @Test
    fun `updateWeight coerces to maximum of 500`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 3, weight = 100f)
        viewModel.updateWeight(ExerciseType.BENCH_PRESS, 600f)

        val selections = viewModel.uiState.value.selections
        assertEquals(500f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    @Test
    fun `updateWeight preserves sets`() {
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 5, weight = 100f)
        viewModel.updateWeight(ExerciseType.BENCH_PRESS, 150f)

        val selections = viewModel.uiState.value.selections
        assertEquals(5, selections[ExerciseType.BENCH_PRESS]?.sets)
        assertEquals(150f, selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    // ========== Template Name Tests ==========

    @Test
    fun `setTemplateName updates template name`() {
        viewModel.setTemplateName("My Workout")

        assertEquals("My Workout", viewModel.uiState.value.templateName)
    }

    @Test
    fun `setTemplateName accepts empty string`() {
        viewModel.setTemplateName("My Workout")
        viewModel.setTemplateName("")

        assertEquals("", viewModel.uiState.value.templateName)
    }

    // ========== Save Template Tests ==========

    @Test
    fun `saveTemplate saves new template successfully`() = runTest {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("My Workout")
        coEvery { mockTemplateRepository.fetchTemplateByName(any()) } returns null
        coEvery { mockTemplateRepository.saveTemplate(any()) } just Runs

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTemplateRepository.saveTemplate(match { it.name == "My Workout" }) }
        assertEquals("", viewModel.uiState.value.templateName)
        assertTrue(viewModel.uiState.value.selections.isEmpty())
        assertFalse(viewModel.uiState.value.editMode)
    }

    @Test
    fun `saveTemplate does nothing when selections are empty`() = runTest {
        viewModel.setTemplateName("My Workout")

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockTemplateRepository.saveTemplate(any()) }
    }

    @Test
    fun `saveTemplate does nothing when template name is blank`() = runTest {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("")

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockTemplateRepository.saveTemplate(any()) }
    }

    @Test
    fun `saveTemplate shows error when duplicate name exists`() = runTest {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("Existing Template")

        val existingTemplate = WorkoutTemplate("other-id", "Existing Template", WorkoutPlan(emptyList()))
        coEvery { mockTemplateRepository.fetchTemplateByName("Existing Template") } returns existingTemplate

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("already exists"))
        coVerify(exactly = 0) { mockTemplateRepository.saveTemplate(any()) }
    }

    @Test
    fun `saveTemplate allows duplicate name when editing same template`() = runTest {
        val existingTemplate = WorkoutTemplate("template-123", "My Workout", WorkoutPlan(emptyList()))
        viewModel.loadTemplate(existingTemplate)
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)

        coEvery { mockTemplateRepository.fetchTemplateByName("My Workout") } returns existingTemplate
        coEvery { mockTemplateRepository.saveTemplate(any()) } just Runs

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        coVerify { mockTemplateRepository.saveTemplate(any()) }
    }

    @Test
    fun `saveTemplate updates existing template in edit mode`() = runTest {
        val existingTemplate = WorkoutTemplate("template-123", "My Workout", WorkoutPlan(emptyList()))
        viewModel.loadTemplate(existingTemplate)
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)

        coEvery { mockTemplateRepository.fetchTemplateByName(any()) } returns null
        coEvery { mockTemplateRepository.saveTemplate(any()) } just Runs

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            mockTemplateRepository.saveTemplate(match {
                it.id == "template-123" && it.name == "My Workout"
            })
        }
    }

    @Test
    fun `saveTemplate sets isSaving to true during save`() = runTest {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("My Workout")
        coEvery { mockTemplateRepository.fetchTemplateByName(any()) } returns null
        coEvery { mockTemplateRepository.saveTemplate(any()) } coAnswers {
            assertTrue(viewModel.uiState.value.isSaving)
        }

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveTemplate handles repository exception`() = runTest {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("My Workout")
        coEvery { mockTemplateRepository.fetchTemplateByName(any()) } returns null
        coEvery { mockTemplateRepository.saveTemplate(any()) } throws Exception("Save failed")

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Save failed", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveTemplate clears error on new save attempt`() = runTest {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("My Workout")
        coEvery { mockTemplateRepository.fetchTemplateByName(any()) } returns null
        coEvery { mockTemplateRepository.saveTemplate(any()) } throws Exception("Save failed")

        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        coEvery { mockTemplateRepository.saveTemplate(any()) } just Runs
        viewModel.saveTemplate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    // ========== Load Template Tests ==========

    @Test
    fun `loadTemplate populates selections and name`() {
        val items = listOf(
            WorkoutPlanItem(ExerciseType.BICEP_CURL, sets = 3, weight = 0f),
            WorkoutPlanItem(ExerciseType.BENCH_PRESS, sets = 5, weight = 100f)
        )
        val template = WorkoutTemplate("template-123", "My Workout", WorkoutPlan(items))

        viewModel.loadTemplate(template)

        assertEquals("My Workout", viewModel.uiState.value.templateName)
        assertEquals(2, viewModel.uiState.value.selections.size)
        assertTrue(viewModel.uiState.value.selections.containsKey(ExerciseType.BICEP_CURL))
        assertTrue(viewModel.uiState.value.selections.containsKey(ExerciseType.BENCH_PRESS))
        assertEquals(3, viewModel.uiState.value.selections[ExerciseType.BICEP_CURL]?.sets)
        assertEquals(100f, viewModel.uiState.value.selections[ExerciseType.BENCH_PRESS]?.weight)
    }

    @Test
    fun `loadTemplate sets edit mode`() {
        val template = WorkoutTemplate("template-123", "My Workout", WorkoutPlan(emptyList()))

        viewModel.loadTemplate(template)

        assertTrue(viewModel.uiState.value.editMode)
        assertEquals("template-123", viewModel.uiState.value.editingTemplateId)
    }

    // ========== Clear Template Tests ==========

    @Test
    fun `clearTemplate resets all state`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.setTemplateName("My Workout")
        val template = WorkoutTemplate("template-123", "Test", WorkoutPlan(emptyList()))
        viewModel.loadTemplate(template)

        viewModel.clearTemplate()

        assertTrue(viewModel.uiState.value.selections.isEmpty())
        assertEquals("", viewModel.uiState.value.templateName)
        assertFalse(viewModel.uiState.value.editMode)
        assertNull(viewModel.uiState.value.editingTemplateId)
    }

    // ========== Delete Template Tests ==========

    @Test
    fun `deleteTemplate calls repository`() = runTest {
        coEvery { mockTemplateRepository.deleteTemplate(any()) } just Runs

        viewModel.deleteTemplate("template-123")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTemplateRepository.deleteTemplate("template-123") }
    }

    // ========== Get Workout Plan Tests ==========

    @Test
    fun `getWorkoutPlan returns plan with selections`() {
        viewModel.toggleExercise(ExerciseType.BICEP_CURL, sets = 3, weight = 0f)
        viewModel.toggleExercise(ExerciseType.BENCH_PRESS, sets = 5, weight = 100f)

        val plan = viewModel.getWorkoutPlan()

        assertNotNull(plan)
        assertEquals(2, plan.items.size)
        assertTrue(plan.items.any { it.exercise == ExerciseType.BICEP_CURL })
        assertTrue(plan.items.any { it.exercise == ExerciseType.BENCH_PRESS })
    }

    @Test
    fun `getWorkoutPlan returns null when no selections`() {
        val plan = viewModel.getWorkoutPlan()

        assertNull(plan)
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state has empty selections`() {
        assertTrue(viewModel.uiState.value.selections.isEmpty())
    }

    @Test
    fun `initial state has empty template name`() {
        assertEquals("", viewModel.uiState.value.templateName)
    }

    @Test
    fun `initial state is not saving`() {
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state is not in edit mode`() {
        assertFalse(viewModel.uiState.value.editMode)
        assertNull(viewModel.uiState.value.editingTemplateId)
    }
}