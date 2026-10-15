package com.pixelfitquest.feature.workout

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Append-only record of rep corrections (merge, ROM, assist, side) so detection
 * can be tuned from real sessions. One JSON object per line in app private
 * storage (`rep_edits.jsonl`). Never uploaded by the app; included in local
 * JSON export as `repEdits` when the user exports. Rotates when the file grows
 * past [MAX_BYTES] so storage stays bounded without breaking [snapshot].
 */
@Singleton
class RepEditLog @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val file = File(context.filesDir, FILE_NAME)

    fun append(action: String, fields: Map<String, String> = emptyMap()) {
        val payload = LinkedHashMap<String, String>(fields.size + 2)
        payload["t"] = System.currentTimeMillis().toString()
        payload["action"] = action
        payload.putAll(fields)
        val line = payload.entries.joinToString(prefix = "{", postfix = "}\n", separator = ",") { (key, value) ->
            "\"${escape(key)}\":\"${escape(value)}\""
        }
        synchronized(LOCK) {
            file.appendText(line)
            rotateIfNeeded()
        }
    }

    /** Current JSONL contents for local export (`repEdits`). Empty if none. */
    fun snapshot(): String = synchronized(LOCK) {
        if (!file.exists()) "" else file.readText()
    }

    private fun rotateIfNeeded() {
        if (!file.exists() || file.length() <= MAX_BYTES) return
        val rotated = rotateJsonlContent(file.readText(), MAX_BYTES)
        file.writeText(rotated)
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(ch)
            }
        }
    }

    companion object {
        const val FILE_NAME = "rep_edits.jsonl"
        /** Soft cap (~500 KiB). Oldest lines are dropped on overflow. */
        const val MAX_BYTES = 500 * 1024
        private val LOCK = Any()

        /**
         * Keep a trailing window of [maxBytes] (or less), cutting at a newline
         * so every remaining line stays valid JSONL.
         */
        fun rotateJsonlContent(text: String, maxBytes: Int): String {
            if (maxBytes <= 0 || text.length <= maxBytes) return text
            val keepFrom = (text.length - maxBytes).coerceAtLeast(0)
            // Prefer cutting after a prior newline so the first kept line is whole JSON.
            val start = if (keepFrom == 0) {
                0
            } else {
                val priorNl = text.lastIndexOf('\n', keepFrom - 1)
                if (priorNl >= 0) priorNl + 1 else keepFrom
            }
            return text.substring(start)
        }
    }
}
