package com.pixelfitquest.model

import android.util.Log
import com.google.gson.Gson
import java.time.Instant

// Assuming WorkoutPlan and WorkoutPlanItem are defined here or imported
data class WorkoutPlanItem(
    val exercise: ExerciseType,
    val sets: Int
)

data class WorkoutPlan(
    val items: List<WorkoutPlanItem>
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): WorkoutPlan = Gson().fromJson(json, WorkoutPlan::class.java)
    }
}

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val plan: WorkoutPlan,
    val createdAt: String? = Instant.now().toString()
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutTemplate {
            val id = map["id"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val createdAt = map["createdAt"] as? String

            val planItems = (map["plan"] as? List<Map<String, Any?>>)?.mapNotNull { itemMap ->
                val exerciseStr = itemMap["exercise"] as? String ?: return@mapNotNull null
                val workoutType = try {
                    val normalized = exerciseStr.uppercase().replace("-", "_")
                    ExerciseType.valueOf(normalized)
                } catch (e: IllegalArgumentException) {
                    null
                }
                val rawSets = itemMap["sets"]
                val sets = when (rawSets) {
                    is Int -> rawSets
                    is Double -> rawSets.toInt()
                    is String -> rawSets.toIntOrNull() ?: 0
                    is Number -> rawSets.toInt()
                    else -> 0
                }.coerceAtLeast(1)
                Log.d("TemplateFromMap", "Item sets raw: $rawSets, parsed: $sets")  // Keep log for debug
                workoutType?.let { WorkoutPlanItem(it, sets) }
            } ?: emptyList()

            if (planItems.isEmpty()) {
                throw IllegalArgumentException("Invalid plan: No valid items found")
            }

            return WorkoutTemplate(
                id = id,
                name = name,
                plan = WorkoutPlan(planItems),
                createdAt = createdAt
            )
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "plan" to plan.items.map { item ->
            mapOf(
                "exercise" to item.exercise.type,
                "sets" to item.sets
            )
        },
        "createdAt" to createdAt
    )
}