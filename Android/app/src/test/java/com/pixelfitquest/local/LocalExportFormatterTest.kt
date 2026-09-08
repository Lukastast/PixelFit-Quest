package com.pixelfitquest.local

import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.local.export.ExerciseExport
import com.pixelfitquest.local.export.LocalExportFormatter
import com.pixelfitquest.local.export.LocalExportSnapshot
import com.pixelfitquest.local.export.WorkoutExport
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalExportFormatterTest {
    private val snapshot = LocalExportSnapshot(
        profile = UserData(level = 2, coins = 15, exp = 40, streak = 3),
        character = CharacterData(gender = "male", variant = "basic"),
        workouts = listOf(
            WorkoutExport(
                workout = Workout(
                    id = "w1",
                    date = "2026-09-08T12:00:00Z",
                    name = "push",
                    totalExercises = 1,
                    totalSets = 1,
                ),
                exercises = listOf(
                    ExerciseExport(
                        exercise = Exercise(
                            id = "e1",
                            workoutId = "w1",
                            type = ExerciseType.BENCH_PRESS,
                            profileId = "bench_press",
                            totalSets = 1,
                            weight = 60f,
                        ),
                        sets = listOf(
                            WorkoutSet(
                                id = "s1",
                                exerciseId = "e1",
                                workoutId = "w1",
                                setNumber = 1,
                                reps = 8,
                                weight = 60f,
                                formScore = 90f,
                                romScore = 88f,
                            )
                        ),
                    )
                ),
            )
        ),
        templates = emptyList(),
        exportedAt = "2026-09-08T12:00:00Z",
    )

    @Test
    fun jsonContainsLocalProfileAndWorkouts() {
        val json = LocalExportFormatter.toJson(snapshot)
        assertTrue(json.contains("\"source\": \"local\""))
        assertTrue(json.contains("\"coins\": 15"))
        assertTrue(json.contains("w1"))
        assertTrue(json.contains("bench-press"))
    }

    @Test
    fun csvHasHeaderAndSetRow() {
        val csv = LocalExportFormatter.toCsv(snapshot)
        val lines = csv.lines()
        assertTrue(lines.first().startsWith("workoutId,date,workoutName"))
        assertTrue(lines.any { it.contains("w1") && it.contains("s1") && it.contains("8") })
    }

    @Test
    fun csvEscapesCommasAndQuotes() {
        assertTrue(LocalExportFormatter.csvEscape("a,b").startsWith("\""))
        assertTrue(LocalExportFormatter.csvEscape("say \"hi\"").contains("\"\""))
    }
}
