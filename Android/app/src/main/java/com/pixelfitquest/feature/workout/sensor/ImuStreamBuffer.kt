package com.pixelfitquest.feature.workout.sensor

/**
 * Growable structure-of-arrays buffer for [TimedVec3] streams.
 *
 * Recording path stores primitives only (no per-sample object). [toList] boxes
 * once at snapshot. [clear] resets size but keeps capacity for the next set.
 * Does not drop samples — growth preserves chronological order.
 */
internal class TimedVec3Buffer(initialCapacity: Int = DEFAULT_CAPACITY) {
    init {
        require(initialCapacity > 0) { "initialCapacity must be > 0" }
    }

    private var tNanos = LongArray(initialCapacity)
    private var x = FloatArray(initialCapacity)
    private var y = FloatArray(initialCapacity)
    private var z = FloatArray(initialCapacity)

    var size: Int = 0
        private set

    val capacity: Int get() = tNanos.size

    val isEmpty: Boolean get() = size == 0

    fun add(t: Long, xv: Float, yv: Float, zv: Float) {
        ensureCapacity(size + 1)
        val i = size
        tNanos[i] = t
        x[i] = xv
        y[i] = yv
        z[i] = zv
        size = i + 1
    }

    fun clear() {
        size = 0
    }

    fun firstTNanos(): Long {
        check(size > 0) { "empty buffer" }
        return tNanos[0]
    }

    fun toList(): List<TimedVec3> {
        val n = size
        if (n == 0) return emptyList()
        val out = ArrayList<TimedVec3>(n)
        var i = 0
        while (i < n) {
            out.add(TimedVec3(tNanos[i], x[i], y[i], z[i]))
            i++
        }
        return out
    }

    private fun ensureCapacity(min: Int) {
        val cur = tNanos.size
        if (min <= cur) return
        var next = cur
        while (next < min) {
            next = next * 2
        }
        tNanos = tNanos.copyOf(next)
        x = x.copyOf(next)
        y = y.copyOf(next)
        z = z.copyOf(next)
    }

    companion object {
        const val DEFAULT_CAPACITY = 4096
    }
}

/**
 * Growable structure-of-arrays buffer for [TimedQuat] streams.
 * Same alloc/reuse contract as [TimedVec3Buffer].
 */
internal class TimedQuatBuffer(initialCapacity: Int = TimedVec3Buffer.DEFAULT_CAPACITY) {
    init {
        require(initialCapacity > 0) { "initialCapacity must be > 0" }
    }

    private var tNanos = LongArray(initialCapacity)
    private var x = FloatArray(initialCapacity)
    private var y = FloatArray(initialCapacity)
    private var z = FloatArray(initialCapacity)
    private var w = FloatArray(initialCapacity)

    var size: Int = 0
        private set

    val capacity: Int get() = tNanos.size

    val isEmpty: Boolean get() = size == 0

    fun add(t: Long, xv: Float, yv: Float, zv: Float, wv: Float) {
        ensureCapacity(size + 1)
        val i = size
        tNanos[i] = t
        x[i] = xv
        y[i] = yv
        z[i] = zv
        w[i] = wv
        size = i + 1
    }

    fun clear() {
        size = 0
    }

    fun toList(): List<TimedQuat> {
        val n = size
        if (n == 0) return emptyList()
        val out = ArrayList<TimedQuat>(n)
        var i = 0
        while (i < n) {
            out.add(TimedQuat(tNanos[i], x[i], y[i], z[i], w[i]))
            i++
        }
        return out
    }

    private fun ensureCapacity(min: Int) {
        val cur = tNanos.size
        if (min <= cur) return
        var next = cur
        while (next < min) {
            next = next * 2
        }
        tNanos = tNanos.copyOf(next)
        x = x.copyOf(next)
        y = y.copyOf(next)
        z = z.copyOf(next)
        w = w.copyOf(next)
    }
}
