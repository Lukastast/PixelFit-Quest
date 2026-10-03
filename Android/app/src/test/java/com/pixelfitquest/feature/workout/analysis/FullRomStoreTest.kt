package com.pixelfitquest.feature.workout.analysis

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FullRomStoreTest {

    private lateinit var prefs: FakeSharedPreferences
    private lateinit var store: FullRomStore

    @Before
    fun setUp() {
        prefs = FakeSharedPreferences()
        store = FullRomStore(prefs)
    }

    @Test
    fun replaceThenConfirmWithTallerPeers_doesNotRaiseAboveBaseline() {
        val exerciseId = "bench_press"
        val baseline = 0.40f
        // User marked 100% ROM on a 0.40 amp rep.
        store.replace(exerciseId, baseline)

        // Accepted peers include taller amps that would previously poison raise().
        store.raiseFromConfirmedSet(
            exerciseId = exerciseId,
            acceptedAmplitudes = listOf(0.40f, 0.48f, 0.52f),
            userRecalibrated = true,
        )

        assertEquals(baseline, store.get(exerciseId)!!, 0.0001f)
    }

    @Test
    fun confirmWithoutRecalibration_raisesToCrediblePeak() {
        val exerciseId = "squat"
        store.replace(exerciseId, 0.30f)

        store.raiseFromConfirmedSet(
            exerciseId = exerciseId,
            acceptedAmplitudes = listOf(0.36f, 0.40f, 0.38f),
            userRecalibrated = false,
        )

        assertEquals(0.40f, store.get(exerciseId)!!, 0.0001f)
    }

    @Test
    fun raiseAlone_stillRaisesWhenNotRecalibratedPath() {
        val exerciseId = "row"
        assertNull(store.get(exerciseId))
        store.raise(exerciseId, 0.25f)
        store.raise(exerciseId, 0.22f)
        assertEquals(0.25f, store.get(exerciseId)!!, 0.0001f)
        store.raise(exerciseId, 0.31f)
        assertEquals(0.31f, store.get(exerciseId)!!, 0.0001f)
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = data
        override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (data[key] as? MutableSet<String> ?: defValues)
        override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(data)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class FakeEditor(private val backingData: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private var clearPending = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) pending[key] = values
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = null
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearPending = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearPending) backingData.clear()
                pending.forEach { (k, v) ->
                    if (v == null) backingData.remove(k) else backingData[k] = v
                }
                pending.clear()
            }
        }
    }
}
