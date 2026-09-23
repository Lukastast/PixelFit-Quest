package com.pixelfitquest.feature.workout.catalog

import com.pixelfitquest.feature.workout.analysis.ExerciseProfiles
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workout.model.enums.displayName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseCatalogTest {

    private val shippedImuIds = setOf(
        "bench-press",
        "overhead-press",
        "seated-overhead-press",
        "close-grip-bench-press",
        "floor-press",
        "incline-bench-press",
        "decline-bench-press",
        "squat",
        "front-squat",
        "romanian-deadlift",
        "good-morning",
        "deadlift",
        "sumo-deadlift",
        "rack-pull",
        "hip-thrust",
        "lat-pulldown",
        "seated-rows",
        "barbell-row",
        "pendlay-row",
        "t-bar-row",
        "bicep-curl",
        "ez-bar-curl",
        "preacher-curl",
        "reverse-curl",
        "skull-crusher",
        "tricep-extension",
    )

    @Test
    fun catalogIsMuchLargerThanTheOriginalFourLifts() {
        assertTrue(
            "catalog size=${ExerciseCatalog.all.size}",
            ExerciseCatalog.all.size >= 80,
        )
    }

    @Test
    fun everyEnumValueHasExactlyOneCatalogRow() {
        val types = ExerciseCatalog.all.map { it.type }
        assertEquals(types.size, types.distinct().size)
        assertEquals(ExerciseType.entries.toSet(), types.toSet())
    }

    @Test
    fun imuFlagMatchesShippedProfilesOnly() {
        val imu = ExerciseCatalog.all.filter { it.imuSupported }
        assertEquals(shippedImuIds, imu.map { it.id }.toSet())
        assertEquals(shippedImuIds.size, imu.size)
        assertTrue(ExerciseCatalog.hasImuSupport(ExerciseType.BENCH_PRESS))
        assertTrue(ExerciseCatalog.hasImuSupport(ExerciseType.SQUAT))
        assertTrue(ExerciseCatalog.hasImuSupport(ExerciseType.BICEP_CURL))
        assertTrue(ExerciseCatalog.hasImuSupport(ExerciseType.DEADLIFT))
        assertTrue(ExerciseCatalog.hasImuSupport(ExerciseType.OVERHEAD_PRESS))
        assertTrue(ExerciseCatalog.hasImuSupport(ExerciseType.CLOSE_GRIP_BENCH_PRESS))
        assertFalse(ExerciseCatalog.hasImuSupport(ExerciseType.LEG_PRESS))
        assertFalse(ExerciseCatalog.hasImuSupport(ExerciseType.DUMBBELL_BENCH_PRESS))
    }

    @Test
    fun everyTrackedLiftHasItsOwnProfile() {
        ExerciseCatalog.all.filter { it.imuSupported }.forEach { def ->
            assertEquals(def.id, ExerciseProfiles.forId(def.id).id)
        }
    }

    @Test
    fun commonGymLiftsArePresentForLogging() {
        val names = ExerciseCatalog.all.map { it.displayName }.toSet()
        listOf(
            "Deadlift",
            "Overhead Press",
            "Pull-Up",
            "Hip Thrust",
            "Lat Pulldown",
            "Leg Press",
            "Romanian Deadlift",
            "Lateral Raise",
            "Plank",
            "Kettlebell Swing",
        ).forEach { expected ->
            assertTrue("missing $expected in $names", expected in names)
        }
    }

    @Test
    fun searchFindsLogOnlyDeadliftAndImuBench() {
        val dead = ExerciseCatalog.search("dead")
        assertTrue(dead.any { it.type == ExerciseType.DEADLIFT && it.imuSupported })
        assertTrue(dead.any { it.type == ExerciseType.ROMANIAN_DEADLIFT && it.imuSupported })

        val bench = ExerciseCatalog.search("bench")
        assertTrue(bench.any { it.type == ExerciseType.BENCH_PRESS && it.imuSupported })
        assertTrue(bench.any { it.type == ExerciseType.INCLINE_BENCH_PRESS && it.imuSupported })
        assertTrue(bench.any { it.type == ExerciseType.DUMBBELL_BENCH_PRESS && !it.imuSupported })
    }

    @Test
    fun categoryAndImuFiltersCompose() {
        val chestImu = ExerciseCatalog.search(
            query = "",
            category = ExerciseCategory.CHEST,
            imuOnly = true,
        )
        assertEquals(
            listOf(
                ExerciseType.BENCH_PRESS,
                ExerciseType.INCLINE_BENCH_PRESS,
                ExerciseType.DECLINE_BENCH_PRESS,
                ExerciseType.FLOOR_PRESS,
            ),
            chestImu.map { it.type },
        )

        val logLegs = ExerciseCatalog.search(
            query = "",
            category = ExerciseCategory.LEGS,
            logOnly = true,
        )
        assertTrue(logLegs.isNotEmpty())
        assertTrue(logLegs.none { it.imuSupported })
        assertTrue(logLegs.any { it.type == ExerciseType.LEG_PRESS })
        assertFalse(logLegs.any { it.type == ExerciseType.SQUAT })
    }

    @Test
    fun fromStoredAcceptsKebabAndSnake() {
        assertEquals(ExerciseType.BENCH_PRESS, ExerciseType.fromStored("bench-press"))
        assertEquals(ExerciseType.BENCH_PRESS, ExerciseType.fromStored("BENCH_PRESS"))
        assertEquals(ExerciseType.T_BAR_ROW, ExerciseType.fromStored("t-bar-row"))
        assertEquals(ExerciseType.DEADLIFT, ExerciseType.fromStored("deadlift"))
        assertEquals(null, ExerciseType.fromStored("not-a-lift"))
        assertEquals(null, ExerciseType.fromStored(""))
    }

    @Test
    fun displayNameUsesCatalogCopy() {
        assertEquals("T-Bar Row", ExerciseType.T_BAR_ROW.displayName())
        assertEquals("Bench Press", ExerciseType.BENCH_PRESS.displayName())
        assertNotNull(ExerciseCatalog.definition(ExerciseType.SQUAT))
    }

    @Test
    fun groupedSkipsEmptyCategories() {
        val grouped = ExerciseCatalog.grouped(
            ExerciseCatalog.search(query = "", category = ExerciseCategory.CORE),
        )
        assertEquals(setOf(ExerciseCategory.CORE), grouped.keys)
        assertTrue(grouped.getValue(ExerciseCategory.CORE).all { it.category == ExerciseCategory.CORE })
    }
}
