package com.pixelfitquest.feature.workout

import org.junit.Assert.assertEquals
import org.junit.Test

class RestTimerPrefsTest {
    @Test
    fun secondsStayOnAFifteenSecondGrid() {
        assertEquals(90, RestTimerPrefs.coerceSeconds(90))
        assertEquals(30, RestTimerPrefs.coerceSeconds(10))
        assertEquals(300, RestTimerPrefs.coerceSeconds(400))
        assertEquals(60, RestTimerPrefs.coerceSeconds(74))
    }
}
