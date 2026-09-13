package com.pixelfitquest.feature.progress

import com.pixelfitquest.feature.progress.model.LiftSetRecord
import com.pixelfitquest.feature.progress.model.ProgressAggregator
import com.pixelfitquest.feature.progress.model.ProgressDataSource
import com.pixelfitquest.feature.progress.model.ProgressSourceResolver
import com.pixelfitquest.feature.progress.model.SampleProgressData
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressAggregatorTest {

    @Test
    fun groupsSetsByWorkoutAndUsesMaxWorkingWeight() {
        val records = listOf(
            rec("s1", "w1", "bench-press", 1_000L, 40f, 8),
            rec("s2", "w1", "bench-press", 2_000L, 45f, 6),
            rec("s3", "w2", "bench-press", 8_000L, 47.5f, 5),
        )
        val series = ProgressAggregator.aggregate(records)
        assertEquals(1, series.size)
        assertEquals(ExerciseType.BENCH_PRESS, series[0].exerciseType)
        assertEquals(2, series[0].sessions.size)
        assertEquals(45f, series[0].sessions[0].workingWeightKg, 0.01f)
        assertEquals(47.5f, series[0].sessions[1].workingWeightKg, 0.01f)
        assertEquals(40f * 8 + 45f * 6, series[0].sessions[0].volumeKg, 0.01f)
        assertEquals(2.5f, series[0].weightDeltaKg!!, 0.01f)
    }

    @Test
    fun splitsDistinctExercisesAndSortsSessionsByTime() {
        val records = listOf(
            rec("c1", "w2", "bicep-curl", 5_000L, 12f, 10),
            rec("b1", "w1", "bench-press", 9_000L, 50f, 5),
            rec("s1", "w1", "squat", 1_000L, 70f, 5),
        )
        val series = ProgressAggregator.aggregate(records)
        assertEquals(
            listOf(ExerciseType.BENCH_PRESS, ExerciseType.SQUAT, ExerciseType.BICEP_CURL),
            series.map { it.exerciseType },
        )
        assertEquals(50f, series[0].sessions.single().workingWeightKg, 0.01f)
        assertEquals(70f, series[1].sessions.single().workingWeightKg, 0.01f)
    }

    @Test
    fun emptyRecordsYieldNoSeries() {
        assertTrue(ProgressAggregator.aggregate(emptyList()).isEmpty())
    }

    @Test
    fun ignoresUnknownExerciseTypes() {
        val records = listOf(
            rec("x", "w", "not-a-lift", 1L, 10f, 5),
            rec("b", "w", "BENCH_PRESS", 2L, 40f, 5),
        )
        val series = ProgressAggregator.aggregate(records)
        assertEquals(listOf(ExerciseType.BENCH_PRESS), series.map { it.exerciseType })
    }

    @Test
    fun resolverPrefersLocalThenWorkoutLogThenSample() {
        val local = listOf(rec("l", "w", "squat", 1L, 80f, 5))
        val remote = listOf(rec("r", "w", "bench-press", 1L, 40f, 5))

        val fromLocal = ProgressSourceResolver.resolve(local, remote)
        assertEquals(ProgressDataSource.LOCAL, fromLocal.source)
        assertEquals(local, fromLocal.records)

        val fromRemote = ProgressSourceResolver.resolve(emptyList(), remote)
        assertEquals(ProgressDataSource.WORKOUT_LOG, fromRemote.source)
        assertEquals(remote, fromRemote.records)

        val sample = ProgressSourceResolver.resolve(emptyList(), emptyList())
        assertEquals(ProgressDataSource.SAMPLE, sample.source)
        assertTrue(sample.records.isNotEmpty())
        assertTrue(sample.records.all { it.workoutId.startsWith("sample-") })
    }

    @Test
    fun sampleDataHasMultipleExercisesAndIncreasingBenchWeight() {
        val now = 1_700_000_000_000L
        val series = ProgressAggregator.aggregate(SampleProgressData.records(now))
        assertTrue(series.size >= 3)
        val bench = series.first { it.exerciseType == ExerciseType.BENCH_PRESS }
        assertEquals(7, bench.sessions.size)
        assertTrue(bench.sessions.last().workingWeightKg > bench.sessions.first().workingWeightKg)
        assertFalse(bench.sessions.any { it.workoutId.isBlank() })
    }

    private fun rec(
        id: String,
        workoutId: String,
        type: String,
        timestamp: Long,
        weight: Float,
        reps: Int,
    ) = LiftSetRecord(
        id = id,
        workoutId = workoutId,
        exerciseType = type,
        timestampMillis = timestamp,
        weightKg = weight,
        reps = reps,
    )
}
