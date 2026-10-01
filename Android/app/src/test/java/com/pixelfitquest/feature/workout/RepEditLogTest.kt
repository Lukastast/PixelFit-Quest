package com.pixelfitquest.feature.workout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepEditLogTest {
    @Test
    fun rotateKeepsTrailingWindowOnNewline() {
        val lines = (1..20).joinToString("") { "{\"n\":\"$it\"}\n" }
        val maxBytes = lines.length / 2
        val rotated = RepEditLog.rotateJsonlContent(lines, maxBytes)
        assertTrue(rotated.length <= maxBytes)
        assertTrue(rotated.startsWith("{"))
        assertTrue(rotated.endsWith("\n"))
        assertTrue(rotated.contains("\"n\":\"20\""))
        assertFalse(rotated.contains("\"n\":\"1\""))
        // Every line is complete JSONL
        rotated.trimEnd().split("\n").forEach { line ->
            assertTrue(line.startsWith("{") && line.endsWith("}"))
        }
    }

    @Test
    fun rotateNoOpWhenUnderCap() {
        val text = "{\"a\":\"1\"}\n{\"a\":\"2\"}\n"
        assertEquals(text, RepEditLog.rotateJsonlContent(text, 500 * 1024))
    }

    @Test
    fun rotateEmptyOrTinyMax() {
        assertEquals("", RepEditLog.rotateJsonlContent("", 100))
        assertEquals("", RepEditLog.rotateJsonlContent("{\"x\":1}\n", 0))
    }
}
