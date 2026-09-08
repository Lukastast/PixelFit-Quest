package com.pixelfitquest.local.export

import com.google.gson.GsonBuilder
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.firebase.model.UserData
import java.time.Instant

data class ExerciseExport(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
)

data class WorkoutExport(
    val workout: Workout,
    val exercises: List<ExerciseExport>,
)

data class LocalExportSnapshot(
    val profile: UserData,
    val character: CharacterData,
    val workouts: List<WorkoutExport>,
    val templates: List<WorkoutTemplate>,
    val exportedAt: String = Instant.now().toString(),
    val schemaVersion: Int = 1,
)

object LocalExportFormatter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(snapshot: LocalExportSnapshot): String {
        val payload = mapOf(
            "exportedAt" to snapshot.exportedAt,
            "schemaVersion" to snapshot.schemaVersion,
            "source" to "local",
            "profile" to mapOf(
                "height" to snapshot.profile.height,
                "armLength" to snapshot.profile.armLength,
                "musicVolume" to snapshot.profile.musicVolume,
                "level" to snapshot.profile.level,
                "coins" to snapshot.profile.coins,
                "exp" to snapshot.profile.exp,
                "streak" to snapshot.profile.streak,
                "character" to mapOf(
                    "gender" to snapshot.character.gender,
                    "variant" to snapshot.character.variant,
                    "unlockedVariants" to snapshot.character.unlockedVariants,
                ),
            ),
            "workouts" to snapshot.workouts.map { item ->
                item.workout.toMap() + mapOf(
                    "exercises" to item.exercises.map { exerciseExport ->
                        (exerciseExport.exercise.toMap()) + mapOf(
                            "sets" to exerciseExport.sets.map { it.toMap() },
                        )
                    },
                )
            },
            "templates" to snapshot.templates.map { it.toMap() },
        )
        return gson.toJson(payload)
    }

    fun toCsv(snapshot: LocalExportSnapshot): String {
        val header = listOf(
            "workoutId",
            "date",
            "workoutName",
            "exerciseId",
            "exerciseType",
            "setId",
            "setNumber",
            "reps",
            "weight",
            "formScore",
            "romScore",
            "userCorrected",
        ).joinToString(",")

        val rows = snapshot.workouts.flatMap { workoutExport ->
            val workout = workoutExport.workout
            if (workoutExport.exercises.isEmpty()) {
                listOf(
                    csvRow(
                        workout.id,
                        workout.date,
                        workout.name,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                    )
                )
            } else {
                workoutExport.exercises.flatMap { exerciseExport ->
                    val exercise = exerciseExport.exercise
                    if (exerciseExport.sets.isEmpty()) {
                        listOf(
                            csvRow(
                                workout.id,
                                workout.date,
                                workout.name,
                                exercise.id,
                                exercise.type.type,
                                "",
                                "",
                                "",
                                exercise.weight.toString(),
                                "",
                                "",
                                "",
                            )
                        )
                    } else {
                        exerciseExport.sets.map { set ->
                            csvRow(
                                workout.id,
                                workout.date,
                                workout.name,
                                exercise.id,
                                exercise.type.type,
                                set.id,
                                set.setNumber.toString(),
                                set.reps.toString(),
                                set.weight.toString(),
                                set.formScore.toString(),
                                set.romScore.toString(),
                                set.userCorrected.toString(),
                            )
                        }
                    }
                }
            }
        }

        return (listOf(header) + rows).joinToString("\n")
    }

    private fun csvRow(vararg cells: String): String =
        cells.joinToString(",") { csvEscape(it) }

    internal fun csvEscape(value: String): String {
        val needsQuotes = value.contains(',') ||
            value.contains('"') ||
            value.contains('\n') ||
            value.contains('\r')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}
