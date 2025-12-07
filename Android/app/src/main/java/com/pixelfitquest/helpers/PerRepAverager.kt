package com.pixelfitquest.helpers

class PerRepAverager(
    private val maxValue: Float = 100f,
    private val invertScore: Boolean = false
) {
    private var total = 0f
    private var count = 0

    fun add(value: Float) {
        val processed = if (invertScore) {
            // For tilt: higher magnitude = worse score
            (value.coerceAtLeast(0f) / maxValue * 100f).coerceAtMost(100f)
        } else {
            value.coerceIn(0f, maxValue)
        }
        total += processed
        count++
    }

    fun finalizeAndGetAverage(): Float {
        val avg = if (count > 0) total / count else 0f
        total = 0f
        count = 0
        return avg
    }

    fun getCurrentAverage(): Float = if (count > 0) total / count else 0f
}