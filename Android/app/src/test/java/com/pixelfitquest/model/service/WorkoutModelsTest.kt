package com.pixelfitquest.model.service.module

import com.pixelfitquest.model.enums.ExerciseType
import com.pixelfitquest.model.workout.Exercise
import com.pixelfitquest.model.workout.Workout
import com.pixelfitquest.model.workout.WorkoutSet
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WorkoutModelsTest {

    // ========== ExerciseType Tests ==========

    @Test
    fun `ExerciseType romFactor returns correct values`() {
        assertEquals(0.28f, ExerciseType.BENCH_PRESS.romFactor)
        assertEquals(0.53f, ExerciseType.SQUAT.romFactor)
        assertEquals(0.15f, ExerciseType.BICEP_CURL.romFactor)
        assertEquals(0.60f, ExerciseType.LAT_PULLDOWN.romFactor)
        assertEquals(0.40f, ExerciseType.SEATED_ROWS.romFactor)
        assertEquals(0.18f, ExerciseType.TRICEP_EXTENSION.romFactor)
    }

    @Test
    fun `ExerciseType type converts to lowercase with hyphens`() {
        assertEquals("bench-press", ExerciseType.BENCH_PRESS.type)
        assertEquals("squat", ExerciseType.SQUAT.type)
        assertEquals("bicep-curl", ExerciseType.BICEP_CURL.type)
        assertEquals("lat-pulldown", ExerciseType.LAT_PULLDOWN.type)
        assertEquals("seated-rows", ExerciseType.SEATED_ROWS.type)
        assertEquals("tricep-extension", ExerciseType.TRICEP_EXTENSION.type)
    }

    // ========== Workout Tests ==========

    @Test
    fun `Workout toMap converts all fields correctly`() {
        val workout = Workout(
            id = "workout-1",
            date = "2024-01-15",
            name = "Chest Day",
            totalExercises = 3,
            totalSets = 12,
            overallScore = 85.5f,
            notes = "Great session",
            rewardsAwarded = true
        )

        val map = workout.toMap()

        assertEquals("workout-1", map["id"])
        assertEquals("2024-01-15", map["date"])
        assertEquals("Chest Day", map["name"])
        assertEquals(3, map["totalExercises"])
        assertEquals(12, map["totalSets"])
        assertEquals(85.5f, map["overallScore"])
        assertEquals("Great session", map["notes"])
        assertEquals(true, map["rewardsAwarded"])
    }

    @Test
    fun `Workout fromMap creates workout with all fields`() {
        val map = mapOf(
            "id" to "workout-1",
            "date" to "2024-01-15",
            "name" to "Chest Day",
            "totalExercises" to 3,
            "totalSets" to 12,
            "overallScore" to 85.5f,
            "notes" to "Great session",
            "rewardsAwarded" to true
        )

        val workout = Workout.fromMap(map)

        assertEquals("workout-1", workout.id)
        assertEquals("2024-01-15", workout.date)
        assertEquals("Chest Day", workout.name)
        assertEquals(3, workout.totalExercises)
        assertEquals(12, workout.totalSets)
        assertEquals(85.5f, workout.overallScore)
        assertEquals("Great session", workout.notes)
        assertEquals(true, workout.rewardsAwarded)
    }

    @Test
    fun `Workout fromMap handles missing optional fields`() {
        val map = mapOf(
            "id" to "workout-1",
            "date" to "2024-01-15",
            "name" to "Chest Day"
        )

        val workout = Workout.fromMap(map)

        assertEquals("workout-1", workout.id)
        assertEquals(0, workout.totalExercises)
        assertEquals(0, workout.totalSets)
        assertEquals(0f, workout.overallScore)
        assertNull(workout.notes)
        assertEquals(false, workout.rewardsAwarded)
    }

    @Test
    fun `Workout fromMap handles empty map with defaults`() {
        val map = emptyMap<String, Any?>()

        val workout = Workout.fromMap(map)

        assertEquals("", workout.id)
        assertEquals("", workout.date)
        assertEquals("", workout.name)
        assertEquals(0, workout.totalExercises)
    }

    // ========== Exercise Tests ==========

    @Test
    fun `Exercise toMap converts all fields correctly`() {
        val exercise = Exercise(
            id = "exercise-1",
            workoutId = "workout-1",
            type = ExerciseType.BENCH_PRESS,
            totalSets = 4,
            weight = 100.5f,
            notes = "Good form"
        )

        val map = exercise.toMap()

        assertEquals("exercise-1", map["id"])
        assertEquals("workout-1", map["workoutId"])
        assertEquals("bench-press", map["type"])
        assertEquals(4, map["totalSets"])
        assertEquals(100.5f, map["weight"])
        assertEquals("Good form", map["notes"])
    }

    @Test
    fun `Exercise fromMap creates exercise with valid data`() {
        val map = mapOf(
            "id" to "exercise-1",
            "workoutId" to "workout-1",
            "type" to "bench-press",
            "totalSets" to 4,
            "weight" to 100.5,
            "notes" to "Good form"
        )

        val exercise = Exercise.fromMap(map)

        assertNotNull(exercise)
        assertEquals("exercise-1", exercise.id)
        assertEquals("workout-1", exercise.workoutId)
        assertEquals(ExerciseType.BENCH_PRESS, exercise.type)
        assertEquals(4, exercise.totalSets)
        assertEquals(100.5f, exercise.weight)
        assertEquals("Good form", exercise.notes)
    }

    @Test
    fun `Exercise fromMap returns null when id is missing`() {
        val map = mapOf(
            "workoutId" to "workout-1",
            "type" to "bench-press"
        )

        val exercise = Exercise.fromMap(map)

        assertNull(exercise)
    }

    @Test
    fun `Exercise fromMap returns null when workoutId is missing`() {
        val map = mapOf(
            "id" to "exercise-1",
            "type" to "bench-press"
        )

        val exercise = Exercise.fromMap(map)

        assertNull(exercise)
    }

    @Test
    fun `Exercise fromMap defaults to BENCH_PRESS for unknown type`() {
        val map = mapOf(
            "id" to "exercise-1",
            "workoutId" to "workout-1",
            "type" to "unknown-exercise"
        )

        val exercise = Exercise.fromMap(map)

        assertNotNull(exercise)
        assertEquals(ExerciseType.BENCH_PRESS, exercise.type)
    }

    @Test
    fun `Exercise fromMap handles missing type field`() {
        val map = mapOf(
            "id" to "exercise-1",
            "workoutId" to "workout-1"
        )

        val exercise = Exercise.fromMap(map)

        assertNotNull(exercise)
        assertEquals(ExerciseType.BENCH_PRESS, exercise.type)
    }

    // ========== WorkoutSet Tests ==========

    @Test
    fun `WorkoutSet toMap converts all fields correctly`() {
        val workoutSet = WorkoutSet(
            id = "set-1",
            exerciseId = "exercise-1",
            workoutId = "workout-1",
            setNumber = 1,
            reps = 10,
            romScore = 95.5f,
            xTiltScore = 85.0f,
            zTiltScore = 90.0f,
            workoutScore = 88.5f,
            avgRepTime = 2.5f,
            verticalAccel = 1.2f,
            weight = 100.0f,
            notes = "Perfect form"
        )

        val map = workoutSet.toMap()

        assertEquals("set-1", map["id"])
        assertEquals("exercise-1", map["exerciseId"])
        assertEquals("workout-1", map["workoutId"])
        assertEquals(1, map["setNumber"])
        assertEquals(10, map["reps"])
        assertEquals(95.5f, map["romScore"])
        assertEquals(85.0f, map["xTiltScore"])
        assertEquals(90.0f, map["zTiltScore"])
        assertEquals(88.5f, map["workoutScore"])
        assertEquals(2.5f, map["avgRepTime"])
        assertEquals(1.2f, map["verticalAccel"])
        assertEquals(100.0f, map["weight"])
        assertEquals("Perfect form", map["notes"])
    }

    @Test
    fun `WorkoutSet fromMap creates set with valid data`() {
        val map = mapOf(
            "id" to "set-1",
            "exerciseId" to "exercise-1",
            "workoutId" to "workout-1",
            "setNumber" to 1,
            "reps" to 10,
            "romScore" to 95.5,
            "xTiltScore" to 85.0,
            "zTiltScore" to 90.0,
            "workoutScore" to 88.5,
            "avgRepTime" to 2.5,
            "verticalAccel" to 1.2,
            "weight" to 100.0,
            "notes" to "Perfect form"
        )

        val workoutSet = WorkoutSet.fromMap(map)

        assertNotNull(workoutSet)
        assertEquals("set-1", workoutSet.id)
        assertEquals("exercise-1", workoutSet.exerciseId)
        assertEquals("workout-1", workoutSet.workoutId)
        assertEquals(1, workoutSet.setNumber)
        assertEquals(10, workoutSet.reps)
        assertEquals(95.5f, workoutSet.romScore)
    }

    @Test
    fun `WorkoutSet fromMap returns null when workoutId is missing`() {
        val map = mapOf(
            "id" to "set-1",
            "exerciseId" to "exercise-1"
        )

        val workoutSet = WorkoutSet.fromMap(map)

        assertNull(workoutSet)
    }

    @Test
    fun `WorkoutSet fromMap returns null when exerciseId is missing`() {
        val map = mapOf(
            "id" to "set-1",
            "workoutId" to "workout-1"
        )

        val workoutSet = WorkoutSet.fromMap(map)

        assertNull(workoutSet)
    }

    @Test
    fun `WorkoutSet fromMap handles string numbers conversion`() {
        val map = mapOf(
            "id" to "set-1",
            "exerciseId" to "exercise-1",
            "workoutId" to "workout-1",
            "setNumber" to "3",
            "reps" to "12",
            "romScore" to "95.5",
            "weight" to "100.5"
        )

        val workoutSet = WorkoutSet.fromMap(map)

        assertNotNull(workoutSet)
        assertEquals(3, workoutSet.setNumber)
        assertEquals(12, workoutSet.reps)
        assertEquals(95.5f, workoutSet.romScore)
        assertEquals(100.5f, workoutSet.weight)
    }

    @Test
    fun `WorkoutSet fromMap handles invalid number strings with defaults`() {
        val map = mapOf(
            "id" to "set-1",
            "exerciseId" to "exercise-1",
            "workoutId" to "workout-1",
            "setNumber" to "invalid",
            "reps" to "not-a-number"
        )

        val workoutSet = WorkoutSet.fromMap(map)

        assertNotNull(workoutSet)
        assertEquals(0, workoutSet.setNumber)
        assertEquals(0, workoutSet.reps)
    }

    @Test
    fun `WorkoutSet fromMap handles missing optional fields with defaults`() {
        val map = mapOf(
            "exerciseId" to "exercise-1",
            "workoutId" to "workout-1"
        )

        val workoutSet = WorkoutSet.fromMap(map)

        assertNotNull(workoutSet)
        assertEquals("", workoutSet.id)
        assertEquals(0, workoutSet.setNumber)
        assertEquals(0, workoutSet.reps)
        assertEquals(0f, workoutSet.romScore)
        assertNull(workoutSet.notes)
    }
}