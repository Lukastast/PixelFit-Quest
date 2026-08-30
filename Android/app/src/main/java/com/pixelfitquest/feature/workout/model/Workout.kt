package com.pixelfitquest.feature.workout.model

data class Workout(
    val id: String,
    val date: String, // Keep as ISO string or timestamp
    val name: String,
    val totalExercises: Int = 0,
    val totalSets: Int = 0,
    val overallScore: Float = 0f,
    val totalVolume: Float = 0f,
    val totalDurationMillis: Long = 0,
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
        "totalVolume" to totalVolume,
        "totalDurationMillis" to totalDurationMillis,
        "notes" to notes,
        "rewardsAwarded" to rewardsAwarded
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Workout = Workout(
            id = map["id"] as? String ?: "",
            date = map["date"] as? String ?: "",
            name = map["name"] as? String ?: "",
            totalExercises = (map["totalExercises"] as? Number)?.toInt() ?: 0,
            totalSets = (map["totalSets"] as? Number)?.toInt() ?: 0,
            overallScore = (map["overallScore"] as? Number)?.toFloat() ?: 0f,
            totalVolume = (map["totalVolume"] as? Number)?.toFloat() ?: 0f,
            totalDurationMillis = (map["totalDurationMillis"] as? Number)?.toLong() ?: 0L,
            notes = map["notes"] as? String,
            rewardsAwarded = map["rewardsAwarded"] as? Boolean ?: false
        )
    }
}
