package com.pixelfitquest.model.workout




data class Workout(
    val id: String,
    val date: String,
    val name: String,
    val totalExercises: Int = 0,
    val totalSets: Int = 0,
    val overallScore: Float = 0f,
    val notes: String? = null,
    val rewardsAwarded: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "date" to date,
        "name" to name,
        "totalExercises" to totalExercises,
        "totalSets" to totalSets,
        "overallScore" to overallScore,
        "notes" to notes,
        "rewardsAwarded" to rewardsAwarded

    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Workout = Workout(
            id = map["id"] as? String ?: "",
            date = map["date"] as? String ?: "",
            name = map["name"] as? String ?: "",
            totalExercises = map["totalExercises"] as? Int ?: 0,
            totalSets = map["totalSets"] as? Int ?: 0,
            overallScore = map["overallScore"] as? Float ?: 0f,
            notes = map["notes"] as? String,
            rewardsAwarded = map["rewardsAwarded"] as? Boolean ?: false
        )
    }
}

