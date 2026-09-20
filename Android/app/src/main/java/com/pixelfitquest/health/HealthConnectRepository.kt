package com.pixelfitquest.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
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

        val (rolling7Start, rolling7End) = HealthTime.rolling7DaysRange()
        val rolling7TimeRange = TimeRangeFilter.between(rolling7Start, rolling7End)

        // Rolling 7-day average for resting heart rate to prevent outliers
        val heartRateBpm: Long? = if (HealthPermissions.hasRestingHeartRateRead(granted)) {
            runCatching {
                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(RestingHeartRateRecord.BPM_AVG),
                        timeRangeFilter = rolling7TimeRange,
                    )
                )
                response[RestingHeartRateRecord.BPM_AVG]
            }.getOrNull()
        } else {
            null
        } ?: if (HealthPermissions.hasHeartRateRead(granted)) {
            runCatching {
                // 1. Collect workout intervals over the 7 days to exclude them from resting calculation
                val workoutIntervals = if (HealthPermissions.hasExerciseRead(granted)) {
                    runCatching {
                        val exercises = client.readRecords(
                            ReadRecordsRequest(
                                recordType = ExerciseSessionRecord::class,
                                timeRangeFilter = rolling7TimeRange,
                            )
                        )
                        exercises.records.map { it.startTime to it.endTime }
                    }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }

                // 2. Collect sleep session intervals over the 7 days (sleep HR is the true resting baseline)
                val sleepIntervals = if (HealthPermissions.hasSleepRead(granted)) {
                    runCatching {
                        val sleepRecords = client.readRecords(
                            ReadRecordsRequest(
                                recordType = SleepSessionRecord::class,
                                timeRangeFilter = rolling7TimeRange,
                            )
                        )
                        sleepRecords.records.map { it.startTime to it.endTime }
                    }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }

                // 3. Read heart rate samples, paginating with most recent first
                val nonWorkoutSamples = mutableListOf<Long>()
                val sleepSamples = mutableListOf<Long>()
                var pageToken: String? = null

                do {
                    val hrRecords = client.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = rolling7TimeRange,
                            pageSize = 5000,
                            pageToken = pageToken,
                            ascendingOrder = false,
                        )
                    )
                    for (record in hrRecords.records) {
                        for (sample in record.samples) {
                            val time = sample.time
                            val bpm = sample.beatsPerMinute
                            if (bpm < 35 || bpm > 220) continue

                            // Exclude any heart rate sample recorded during a workout
                            val inWorkout = workoutIntervals.any { (wStart, wEnd) ->
                                !time.isBefore(wStart) && !time.isAfter(wEnd)
                            }
                            if (inWorkout) continue

                            nonWorkoutSamples.add(bpm)

                            // Check if sample was during sleep
                            val inSleep = sleepIntervals.any { (sStart, sEnd) ->
                                !time.isBefore(sStart) && !time.isAfter(sEnd)
                            }
                            if (inSleep) {
                                sleepSamples.add(bpm)
                            }
                        }
                    }
                    pageToken = hrRecords.pageToken
                } while (pageToken != null && nonWorkoutSamples.size < 10000)

                // If sleep HR samples exist, use the 25th percentile of sleep HR (clinical resting HR standard)
                if (sleepSamples.isNotEmpty()) {
                    val sortedSleep = sleepSamples.sorted()
                    val index = (sortedSleep.size * 0.25).toInt().coerceIn(0, sortedSleep.lastIndex)
                    sortedSleep[index]
                } else if (nonWorkoutSamples.isNotEmpty()) {
                    val sortedNonWorkout = nonWorkoutSamples.sorted()
                    val index = (sortedNonWorkout.size * 0.15).toInt().coerceIn(0, sortedNonWorkout.lastIndex)
                    val result = sortedNonWorkout[index]
                    // Sanity check: true resting HR is rarely >= 95 bpm unless only high-intensity data was recorded
                    if (result < 95) result else null
                } else {
                    null
                }
            }.onFailure { Log.w(TAG, "Rolling 7-day heart rate read failed", it) }
                .getOrNull()
        } else {
            null
        }

        val sleepMinutes = if (HealthPermissions.hasSleepRead(granted)) {
            runCatching {
                val lookbackStart = start.minus(Duration.ofHours(14))
                val sleepRange = TimeRangeFilter.between(lookbackStart, end)
                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = SleepSessionRecord::class,
                        timeRangeFilter = sleepRange,
                    )
                )
                val todaySessions = response.records.filter { record ->
                    record.endTime.isAfter(start) && !record.startTime.isAfter(end)
                }
                if (todaySessions.isNotEmpty()) {
                    todaySessions.sumOf { record ->
                        Duration.between(record.startTime, record.endTime).toMinutes()
                    }
                } else {
                    val aggregateResponse = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                            timeRangeFilter = timeRange,
                        )
                    )
                    aggregateResponse[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes()
                }
            }.onFailure { Log.w(TAG, "Sleep read failed", it) }
                .getOrNull()
        } else {
            null
        }

        val (weekStart, weekEnd) = HealthTime.weekRange()
        val weekTimeRange = TimeRangeFilter.between(weekStart, weekEnd)
        var weeklyHeartPoints = 0

        if (HealthPermissions.hasExerciseRead(granted)) {
            runCatching {
                val exerciseRecords = client.readRecords(
                    ReadRecordsRequest(
                        recordType = ExerciseSessionRecord::class,
                        timeRangeFilter = weekTimeRange,
                    )
                )
                var exercisePoints = 0
                for (session in exerciseRecords.records) {
                    val minutes = Duration.between(session.startTime, session.endTime).toMinutes().toInt()
                    exercisePoints += minutes.coerceAtLeast(0)
                }
                weeklyHeartPoints = maxOf(weeklyHeartPoints, exercisePoints)
            }.onFailure { Log.w(TAG, "Exercise sessions read failed", it) }
        }

        if (HealthPermissions.hasHeartRateRead(granted)) {
            runCatching {
                val hrRecords = client.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = weekTimeRange,
                    )
                )
                // Bucket raw samples into distinct minutes (epochSecond / 60)
                // Trackers log samples every 1-5 seconds.
                // To match Google Fit's Heart Points, we use the average BPM across each minute:
                // Moderate intensity (110 - 139 BPM average) = 1 Heart Point / min
                // Vigorous intensity (>= 140 BPM average) = 2 Heart Points / min
                val minuteSamples = mutableMapOf<Long, MutableList<Long>>()
                for (hr in hrRecords.records) {
                    for (sample in hr.samples) {
                        val minute = sample.time.epochSecond / 60
                        minuteSamples.getOrPut(minute) { mutableListOf() }.add(sample.beatsPerMinute)
                    }
                }
                var hrHeartPoints = 0
                for ((_, samples) in minuteSamples) {
                    if (samples.isNotEmpty()) {
                        val avgBpm = samples.average()
                        if (avgBpm >= 140.0) {
                            hrHeartPoints += 2
                        } else if (avgBpm >= 110.0) {
                            hrHeartPoints += 1
                        }
                    }
                }
                weeklyHeartPoints = maxOf(weeklyHeartPoints, hrHeartPoints)
            }.onFailure { Log.w(TAG, "Heart rate read for week failed", it) }
        }

        return HealthMetrics(
            steps = steps,
            stepGoal = HealthMetrics.DEFAULT_STEP_GOAL,
            heartRateBpm = heartRateBpm,
            sleepMinutes = sleepMinutes,
            weeklyHeartPoints = weeklyHeartPoints,
            weeklyHeartGoal = HealthMetrics.DEFAULT_WEEKLY_HEART_GOAL,
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
