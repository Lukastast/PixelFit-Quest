package com.pixelfitquest.feature.workout.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM checks that SoA buffers preserve chronological sample sequences
 * (identical to ArrayList += TimedVec3/Quat) across growth and clear/reuse.
 */
class ImuStreamBufferTest {

    @Test
    fun vec3ToListMatchesAppendOrder() {
        val buf = TimedVec3Buffer(initialCapacity = 4)
        val expected = ArrayList<TimedVec3>()
        for (i in 0 until 10) {
            val sample = TimedVec3(i * 1_000_000L, i.toFloat(), i + 0.5f, i + 1f)
            expected.add(sample)
            buf.add(sample.tNanos, sample.x, sample.y, sample.z)
        }
        assertEquals(expected, buf.toList())
        assertEquals(10, buf.size)
        assertTrue(buf.capacity >= 10)
    }

    @Test
    fun quatToListMatchesAppendOrder() {
        val buf = TimedQuatBuffer(initialCapacity = 2)
        val expected = listOf(
            TimedQuat(0L, 0f, 0f, 0f, 1f),
            TimedQuat(5L, 0.1f, 0.2f, 0.3f, 0.9f),
            TimedQuat(10L, -0.1f, 0f, 0f, 1f),
        )
        for (q in expected) {
            buf.add(q.tNanos, q.x, q.y, q.z, q.w)
        }
        assertEquals(expected, buf.toList())
    }

    @Test
    fun clearReusesCapacityAndPreservesNextSequence() {
        val buf = TimedVec3Buffer(initialCapacity = 8)
        repeat(20) { i -> buf.add(i.toLong(), 1f, 2f, 3f) }
        val capAfterGrow = buf.capacity
        assertTrue(capAfterGrow >= 20)

        buf.clear()
        assertEquals(0, buf.size)
        assertEquals(capAfterGrow, buf.capacity)
        assertTrue(buf.isEmpty)

        val expected = listOf(
            TimedVec3(100L, 9f, 8f, 7f),
            TimedVec3(200L, 6f, 5f, 4f),
        )
        for (s in expected) buf.add(s.tNanos, s.x, s.y, s.z)
        assertEquals(expected, buf.toList())
        assertEquals(100L, buf.firstTNanos())
    }

    @Test
    fun emptyToListIsEmpty() {
        assertEquals(emptyList<TimedVec3>(), TimedVec3Buffer().toList())
        assertEquals(emptyList<TimedQuat>(), TimedQuatBuffer().toList())
    }

    @Test
    fun growthDoesNotDropOrReorderSamples() {
        val buf = TimedVec3Buffer(initialCapacity = 1)
        val n = 4097
        for (i in 0 until n) {
            buf.add(i.toLong(), i.toFloat(), -i.toFloat(), i * 0.25f)
        }
        val list = buf.toList()
        assertEquals(n, list.size)
        for (i in 0 until n) {
            assertEquals(i.toLong(), list[i].tNanos)
            assertEquals(i.toFloat(), list[i].x)
            assertEquals(-i.toFloat(), list[i].y)
            assertEquals(i * 0.25f, list[i].z)
        }
    }
}
