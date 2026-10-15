package com.pixelfitquest.feature.workout.sensor

/**
 * Keep the newest [MAX_TRACES] and stay within [MAX_BYTES]. Oldest go first.
 * The newest entry is always kept, even if it alone exceeds [MAX_BYTES].
 */
object TraceRetention {
    const val MAX_TRACES = 40
    const val MAX_BYTES: Long = 200L * 1024 * 1024

    data class Entry(val name: String, val modified: Long, val bytes: Long)

    fun toDelete(
        entries: List<Entry>,
        maxTraces: Int = MAX_TRACES,
        maxBytes: Long = MAX_BYTES,
    ): List<Entry> {
        val newestFirst = entries.sortedWith(NEWEST)
        val keep = HashSet<String>(minOf(maxTraces.coerceAtLeast(0), newestFirst.size))
        var total = 0L
        for (entry in newestFirst) {
            if (keep.size >= maxTraces) break
            if (keep.isNotEmpty() && total + entry.bytes > maxBytes) break
            keep += entry.name
            total += entry.bytes
        }
        return entries.filter { it.name !in keep }.sortedWith(OLDEST)
    }

    private val NEWEST =
        compareByDescending<Entry> { it.modified }.thenByDescending { it.name }
    private val OLDEST = compareBy<Entry> { it.modified }.thenBy { it.name }
}
