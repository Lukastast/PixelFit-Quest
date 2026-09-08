package com.pixelfitquest.health

interface HealthRepository {
    fun availability(): HealthConnectStatus
    suspend fun grantedPermissions(): Set<String>
    suspend fun readTodayMetrics(): HealthMetrics
    suspend fun writeExerciseSession(session: HealthExerciseWrite): HealthWriteResult
}
