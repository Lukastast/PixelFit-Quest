package com.pixelfitquest.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LocalJson {
    val gson: Gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any?>>() {}.type

    fun toJson(map: Map<String, Any?>): String = gson.toJson(map)

    fun toMap(json: String): Map<String, Any?> {
        if (json.isBlank()) return emptyMap()
        return gson.fromJson(json, mapType) ?: emptyMap()
    }
}
