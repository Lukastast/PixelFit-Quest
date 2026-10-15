package com.pixelfitquest.feature.workout

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutWeightPrefsTest {

    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
    }

    @Test
    fun defaultPreWorkoutWeightCheckIsEnabled() {
        assertTrue(WorkoutWeightPrefs.isEnabled(fakePrefs))
    }

    @Test
    fun disablingPreWorkoutCheckUpdatesPreference() {
        WorkoutWeightPrefs.setEnabled(fakePrefs, false)
        assertFalse(WorkoutWeightPrefs.isEnabled(fakePrefs))
        assertFalse(fakePrefs.getBoolean(WorkoutWeightPrefs.KEY_PRE_WORKOUT_CHECK_ENABLED, true))
    }

    @Test
    fun enablingPreWorkoutCheckUpdatesPreference() {
        WorkoutWeightPrefs.setEnabled(fakePrefs, false)
        assertFalse(WorkoutWeightPrefs.isEnabled(fakePrefs))

        WorkoutWeightPrefs.setEnabled(fakePrefs, true)
        assertTrue(WorkoutWeightPrefs.isEnabled(fakePrefs))
        assertTrue(fakePrefs.getBoolean(WorkoutWeightPrefs.KEY_PRE_WORKOUT_CHECK_ENABLED, false))
    }

    @Test
    fun defaultWeightSuggestionIsEnabled() {
        assertTrue(WorkoutWeightPrefs.isWeightSuggestionEnabled(fakePrefs))
    }

    @Test
    fun togglingWeightSuggestionUpdatesPreference() {
        WorkoutWeightPrefs.setWeightSuggestionEnabled(fakePrefs, false)
        assertFalse(WorkoutWeightPrefs.isWeightSuggestionEnabled(fakePrefs))

        WorkoutWeightPrefs.setWeightSuggestionEnabled(fakePrefs, true)
        assertTrue(WorkoutWeightPrefs.isWeightSuggestionEnabled(fakePrefs))
    }

    @Test
    fun defaultRepThresholdIsTen() {
        assertEquals(10, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
    }

    @Test
    fun updatingRepThresholdWithinBounds() {
        WorkoutWeightPrefs.setRepThreshold(fakePrefs, 12)
        assertEquals(12, WorkoutWeightPrefs.getRepThreshold(fakePrefs))

        WorkoutWeightPrefs.setRepThreshold(fakePrefs, 8)
        assertEquals(8, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
    }

    @Test
    fun repThresholdIsCoercedToValidRange() {
        WorkoutWeightPrefs.setRepThreshold(fakePrefs, 2)
        assertEquals(5, WorkoutWeightPrefs.getRepThreshold(fakePrefs))

        WorkoutWeightPrefs.setRepThreshold(fakePrefs, 50)
        assertEquals(30, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
    }

    @Test
    fun defaultProgressionPresetIsHypertrophy() {
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.getProgressionPreset(fakePrefs))
    }

    @Test
    fun progressionPresetClassification() {
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.STRENGTH, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(5))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(10))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(11))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(12))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.CUSTOM, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(6))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.CUSTOM, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(8))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.CUSTOM, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(15))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.CUSTOM, WorkoutWeightPrefs.ProgressionPreset.fromThreshold(30))
    }

    @Test
    fun applyStrengthPresetUpdatesPreference() {
        WorkoutWeightPrefs.setRepThreshold(fakePrefs, 10)
        WorkoutWeightPrefs.applyStrengthPreset(fakePrefs)
        assertEquals(5, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.STRENGTH, WorkoutWeightPrefs.getProgressionPreset(fakePrefs))
    }

    @Test
    fun applyHypertrophyPresetTogglesBetweenTenAndTwelve() {
        WorkoutWeightPrefs.setRepThreshold(fakePrefs, 5)
        WorkoutWeightPrefs.applyHypertrophyPreset(fakePrefs)
        assertEquals(10, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.getProgressionPreset(fakePrefs))

        WorkoutWeightPrefs.applyHypertrophyPreset(fakePrefs)
        assertEquals(12, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.getProgressionPreset(fakePrefs))

        WorkoutWeightPrefs.applyHypertrophyPreset(fakePrefs)
        assertEquals(10, WorkoutWeightPrefs.getRepThreshold(fakePrefs))
        assertEquals(WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY, WorkoutWeightPrefs.getProgressionPreset(fakePrefs))
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
