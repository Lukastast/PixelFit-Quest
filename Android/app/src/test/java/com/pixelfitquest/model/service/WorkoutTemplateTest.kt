package com.pixelfitquest.model.service.module

import com.pixelfitquest.model.enums.ExerciseType
import com.pixelfitquest.model.workout.WorkoutPlan
import com.pixelfitquest.model.workout.WorkoutPlanItem
import com.pixelfitquest.model.workout.WorkoutTemplate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WorkoutTemplateTest {

    // ========== WorkoutPlan JSON Tests ==========

    @Test
    fun `WorkoutPlan toJson serializes correctly`() {
        val plan = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, 3, 100f),
                WorkoutPlanItem(ExerciseType.SQUAT, 4, 150f)
            )
        )

        val json = plan.toJson()

        assertTrue(json.contains("BENCH_PRESS"))
        assertTrue(json.contains("SQUAT"))
        assertTrue(json.contains("\"sets\":3"))
        assertTrue(json.contains("\"sets\":4"))
    }

    @Test
    fun `WorkoutPlan fromJson deserializes correctly`() {
        val json = """{"items":[{"exercise":"BENCH_PRESS","sets":3,"weight":100.0},{"exercise":"SQUAT","sets":4,"weight":150.0}]}"""

        val plan = WorkoutPlan.fromJson(json)

        assertEquals(2, plan.items.size)
        assertEquals(ExerciseType.BENCH_PRESS, plan.items[0].exercise)
        assertEquals(3, plan.items[0].sets)
        assertEquals(100f, plan.items[0].weight)
    }

    @Test
    fun `WorkoutPlan round-trip serialization preserves data`() {
        val original = WorkoutPlan(
            items = listOf(
                WorkoutPlanItem(ExerciseType.BENCH_PRESS, 3, 100f),
                WorkoutPlanItem(ExerciseType.SQUAT, 4, 150f)
            )
        )

        val json = original.toJson()
        val deserialized = WorkoutPlan.fromJson(json)

        assertEquals(original.items.size, deserialized.items.size)
        assertEquals(original.items[0].exercise, deserialized.items[0].exercise)
        assertEquals(original.items[0].sets, deserialized.items[0].sets)
        assertEquals(original.items[0].weight, deserialized.items[0].weight)
    }

    // ========== WorkoutTemplate fromMap Tests ==========

    @Test
    fun `fromMap creates template with valid data`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Upper Body",
            "createdAt" to "2024-01-15T10:00:00Z",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "lat-pulldown",
                    "sets" to 4,
                    "weight" to 80.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals("template-1", template.id)
        assertEquals("Upper Body", template.name)
        assertEquals("2024-01-15T10:00:00Z", template.createdAt)
        assertEquals(2, template.plan.items.size)
        assertEquals(ExerciseType.BENCH_PRESS, template.plan.items[0].exercise)
        assertEquals(3, template.plan.items[0].sets)
        assertEquals(100f, template.plan.items[0].weight)
    }

    @Test
    fun `fromMap normalizes exercise names correctly`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",  // lowercase with hyphen
                    "sets" to 3,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "SQUAT",  // uppercase
                    "sets" to 4,
                    "weight" to 150.0
                ),
                mapOf(
                    "exercise" to "bicep-curl",  // lowercase with hyphen
                    "sets" to 2,
                    "weight" to 50.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(ExerciseType.BENCH_PRESS, template.plan.items[0].exercise)
        assertEquals(ExerciseType.SQUAT, template.plan.items[1].exercise)
        assertEquals(ExerciseType.BICEP_CURL, template.plan.items[2].exercise)
    }

    @Test
    fun `fromMap handles sets as Int`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 5,  // Int
                    "weight" to 100.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(5, template.plan.items[0].sets)
    }

    @Test
    fun `fromMap handles sets as Double`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 4.0,  // Double
                    "weight" to 100.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(4, template.plan.items[0].sets)
    }

    @Test
    fun `fromMap handles sets as String`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to "3",  // String
                    "weight" to 100.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(3, template.plan.items[0].sets)
    }

    @Test
    fun `fromMap coerces zero or negative sets to 1`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 0,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "squat",
                    "sets" to -5,
                    "weight" to 150.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(1, template.plan.items[0].sets)
        assertEquals(1, template.plan.items[1].sets)
    }

    @Test
    fun `fromMap handles weight as Float`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.5f
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(100.5f, template.plan.items[0].weight)
    }

    @Test
    fun `fromMap handles weight as Double`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.5
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(100.5f, template.plan.items[0].weight)
    }

    @Test
    fun `fromMap handles weight as Int`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(100f, template.plan.items[0].weight)
    }

    @Test
    fun `fromMap handles weight as String`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to "100.5"
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(100.5f, template.plan.items[0].weight)
    }

    @Test
    fun `fromMap handles invalid weight string with default`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to "not-a-number"
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(0f, template.plan.items[0].weight)
    }

    @Test
    fun `fromMap skips items with invalid exercise names`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "invalid-exercise",  // Invalid
                    "sets" to 4,
                    "weight" to 150.0
                ),
                mapOf(
                    "exercise" to "squat",
                    "sets" to 5,
                    "weight" to 200.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals(2, template.plan.items.size)  // Invalid item skipped
        assertEquals(ExerciseType.BENCH_PRESS, template.plan.items[0].exercise)
        assertEquals(ExerciseType.SQUAT, template.plan.items[1].exercise)
    }

    @Test
    fun `fromMap throws exception when all items are invalid`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "invalid-1",
                    "sets" to 3,
                    "weight" to 100.0
                ),
                mapOf(
                    "exercise" to "invalid-2",
                    "sets" to 4,
                    "weight" to 150.0
                )
            )
        )

        assertFailsWith<IllegalArgumentException> {
            WorkoutTemplate.fromMap(map)
        }
    }

    @Test
    fun `fromMap throws exception when plan is empty list`() {
        val map = mapOf(
            "id" to "template-1",
            "name" to "Test",
            "plan" to emptyList<Map<String, Any?>>()
        )

        assertFailsWith<IllegalArgumentException> {
            WorkoutTemplate.fromMap(map)
        }
    }

    @Test
    fun `fromMap handles missing id with empty string`() {
        val map = mapOf(
            "name" to "Test",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals("", template.id)
    }

    @Test
    fun `fromMap handles missing name with empty string`() {
        val map = mapOf(
            "id" to "template-1",
            "plan" to listOf(
                mapOf(
                    "exercise" to "bench-press",
                    "sets" to 3,
                    "weight" to 100.0
                )
            )
        )

        val template = WorkoutTemplate.fromMap(map)

        assertEquals("", template.name)
    }

    // ========== WorkoutTemplate toMap Tests ==========

    @Test
    fun `toMap converts template correctly`() {
        val template = WorkoutTemplate(
            id = "template-1",
            name = "Upper Body",
            plan = WorkoutPlan(
                items = listOf(
                    WorkoutPlanItem(ExerciseType.BENCH_PRESS, 3, 100f),
                    WorkoutPlanItem(ExerciseType.LAT_PULLDOWN, 4, 80f)
                )
            ),
            createdAt = "2024-01-15T10:00:00Z"
        )

        val map = template.toMap()

        assertEquals("template-1", map["id"])
        assertEquals("Upper Body", map["name"])
        assertEquals("2024-01-15T10:00:00Z", map["createdAt"])

        val planList = map["plan"] as List<Map<String, Any?>>
        assertEquals(2, planList.size)
        assertEquals("bench-press", planList[0]["exercise"])
        assertEquals(3, planList[0]["sets"])
        assertEquals(100f, planList[0]["weight"])
    }

    @Test
    fun `toMap and fromMap round-trip preserves data`() {
        val original = WorkoutTemplate(
            id = "template-1",
            name = "Full Body",
            plan = WorkoutPlan(
                items = listOf(
                    WorkoutPlanItem(ExerciseType.BENCH_PRESS, 3, 100f),
                    WorkoutPlanItem(ExerciseType.SQUAT, 4, 150f),
                    WorkoutPlanItem(ExerciseType.BICEP_CURL, 2, 50f)
                )
            ),
            createdAt = "2024-01-15T10:00:00Z"
        )

        val map = original.toMap()
        val deserialized = WorkoutTemplate.fromMap(map)

        assertEquals(original.id, deserialized.id)
        assertEquals(original.name, deserialized.name)
        assertEquals(original.createdAt, deserialized.createdAt)
        assertEquals(original.plan.items.size, deserialized.plan.items.size)
        assertEquals(original.plan.items[0].exercise, deserialized.plan.items[0].exercise)
        assertEquals(original.plan.items[0].sets, deserialized.plan.items[0].sets)
        assertEquals(original.plan.items[0].weight, deserialized.plan.items[0].weight)
    }
}