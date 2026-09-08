package com.pixelfitquest.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthRepository {

    override fun availability(): HealthConnectStatus {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectStatus.UPDATE_REQUIRED
            else -> HealthConnectStatus.UNAVAILABLE
        }
    }

    override suspend fun grantedPermissions(): Set<String> {
        val client = clientOrNull() ?: return emptySet()
        return runCatching { client.permissionController.getGrantedPermissions() }
            .getOrDefault(emptySet())
    }

    override suspend fun readTodayMetrics(): HealthMetrics {
        val client = clientOrNull() ?: return HealthMetrics.EMPTY
        val granted = grantedPermissions()
        val (start, end) = HealthTime.todayRange()
        val timeRange = TimeRangeFilter.between(start, end)

        val steps = if (HealthPermissions.hasStepsRead(granted)) {
            runCatching {
                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = timeRange,
                    )
                )
                response[StepsRecord.COUNT_TOTAL] ?: 0L
            }.onFailure { Log.w(TAG, "Steps aggregate failed", it) }
                .getOrDefault(0L)
        } else {
            0L
        }

        val heartRateBpm = if (
            HealthPermission.getReadPermission(HeartRateRecord::class) in granted
        ) {
            runCatching {
                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(HeartRateRecord.BPM_AVG),
                        timeRangeFilter = timeRange,
                    )
                )
                response[HeartRateRecord.BPM_AVG]
            }.onFailure { Log.w(TAG, "Heart rate aggregate failed", it) }
                .getOrNull()
        } else {
            null
        }

        return HealthMetrics(
            steps = steps,
            stepGoal = HealthMetrics.DEFAULT_STEP_GOAL,
            heartRateBpm = heartRateBpm,
        )
    }

    override suspend fun writeExerciseSession(session: HealthExerciseWrite): HealthWriteResult {
        Log.i(
            TAG,
            "writeExerciseSession stub — not inserting into Health Connect: " +
                "title=${session.title} start=${session.startTime} end=${session.endTime}",
        )
        return HealthWriteResult.Stubbed
    }

    private fun clientOrNull(): HealthConnectClient? {
        return if (availability() == HealthConnectStatus.AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    private companion object {
        const val TAG = "HealthConnect"
    }
}
