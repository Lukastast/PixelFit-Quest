package com.pixelfitquest.feature.workout

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Append-only record of rep corrections (merge, ROM, assist, side) so detection
 * can be tuned from real sessions. One JSON object per line in app storage.
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
        }
    }

    fun snapshot(): String = synchronized(LOCK) {
        if (!file.exists()) "" else file.readText()
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

    private companion object {
        const val FILE_NAME = "rep_edits.jsonl"
        val LOCK = Any()
    }
}
