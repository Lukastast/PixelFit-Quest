package com.pixelfitquest.model

import java.time.Instant

// Assuming WorkoutPlan and WorkoutPlanItem are defined here or imported
data class WorkoutPlanItem(
    val exercise: WorkoutType,
    val sets: Int
)

data class WorkoutPlan(
    val items: List<WorkoutPlanItem>
)

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val plan: WorkoutPlan,
    val createdAt: String? = Instant.now().toString()  // ISO for sorting
) {
    companion object {
        // Factory for deserialization (call as WorkoutTemplate.fromMap(map))
        fun fromMap(map: Map<String, Any?>): WorkoutTemplate {
            val id = map["id"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val createdAt = map["createdAt"] as? String

            // Parse plan from nested list of maps
            val planItems = (map["plan"] as? List<Map<String, Any?>>)?.mapNotNull { itemMap ->
                val exerciseStr = itemMap["exercise"] as? String ?: return@mapNotNull null
                val workoutType = try {
                    val normalized = exerciseStr.uppercase().replace("-", "_")
                    WorkoutType.valueOf(normalized)
                } catch (e: IllegalArgumentException) {
                    null  // Skip invalid
                }
                val sets = (itemMap["sets"] as? Int ?: 0).coerceAtLeast(1)
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

    // Instance method for serialization (call as template.toMap())
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "plan" to plan.items.map { item ->
            mapOf(
                "exercise" to item.exercise,  // Uses type getter from WorkoutType/Workout
                "sets" to item.sets
            )
        },
        "createdAt" to createdAt
    )
}