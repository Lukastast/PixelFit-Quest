package com.pixelfitquest.feature.workoutBuilder.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixelfitquest.feature.customization.model.CustomizationUiState
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTemplateHubTest {

    @Test
    fun workoutPlan_serializesAndDeserializesCorrectly() {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(exercise = ExerciseType.BENCH_PRESS, sets = 4, weight = 82.5f),
                WorkoutPlanItem(exercise = ExerciseType.INCLINE_DUMBBELL_PRESS, sets = 3, weight = 26f),
                WorkoutPlanItem(exercise = ExerciseType.TRICEP_EXTENSION, sets = 3, weight = 30f),
            )
        )

        val json = plan.toJson()
        val restored = WorkoutPlan.fromJson(json)

        assertNotNull(restored)
        assertEquals(3, restored.items.size)
        assertEquals(ExerciseType.BENCH_PRESS, restored.items[0].exercise)
        assertEquals(4, restored.items[0].sets)
        assertEquals(82.5f, restored.items[0].weight, 0.001f)
    }

    @Test
    fun workoutPlan_navigationJsonTypeToken_matchesAppScaffoldLogic() {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(exercise = ExerciseType.SQUAT, sets = 5, weight = 120f),
                WorkoutPlanItem(exercise = ExerciseType.LEG_PRESS, sets = 4, weight = 200f),
            )
        )

        val gson = Gson()
        val json = gson.toJson(plan)
        val type = object : TypeToken<WorkoutPlan>() {}.type
        val restored: WorkoutPlan = gson.fromJson(json, type)

        assertEquals(2, restored.items.size)
        assertEquals(ExerciseType.SQUAT, restored.items[0].exercise)
        assertEquals(5, restored.items[0].sets)
    }

    @Test
    fun workoutTemplate_calculatesStatsCorrectly() {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(exercise = ExerciseType.PULL_UP, sets = 4, weight = 0f),
                WorkoutPlanItem(exercise = ExerciseType.LAT_PULLDOWN, sets = 3, weight = 60f),
                WorkoutPlanItem(exercise = ExerciseType.BICEP_CURL, sets = 3, weight = 15f),
            )
        )
        val template = WorkoutTemplate(
            id = "tmpl-1",
            name = "Pull Day",
            plan = plan
        )

        val exerciseCount = template.plan.items.size
        val totalSets = template.plan.items.sumOf { it.sets.coerceAtLeast(1) }

        assertEquals(3, exerciseCount)
        assertEquals(10, totalSets)
    }

    @Test
    fun customizationUiState_saveSuccessAndEditModeTransitions() {
        var state = CustomizationUiState()
        assertFalse(state.saveSuccess)
        assertFalse(state.editMode)
        assertTrue(state.selections.isEmpty())

        // Add selections
        val planItem = WorkoutPlanItem(exercise = ExerciseType.BENCH_PRESS, sets = 3, weight = 60f)
        state = state.copy(
            selections = mapOf(ExerciseType.BENCH_PRESS to planItem),
            templateName = "Chest Day",
            editMode = true,
            editingTemplateId = "t1"
        )
        assertEquals("Chest Day", state.templateName)
        assertTrue(state.editMode)
        assertEquals(1, state.selections.size)

        // Simulate successful save
        state = state.copy(
            isSaving = false,
            error = null,
            editMode = false,
            editingTemplateId = null,
            selections = emptyMap(),
            templateName = "",
            saveSuccess = true
        )
        assertTrue(state.saveSuccess)
        assertFalse(state.editMode)
        assertTrue(state.selections.isEmpty())

        // Reset save success
        state = state.copy(saveSuccess = false)
        assertFalse(state.saveSuccess)
    }
}
