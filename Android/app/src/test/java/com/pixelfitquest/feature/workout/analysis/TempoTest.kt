package com.pixelfitquest.feature.workout.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TempoTest {

    @Test
    fun longerEccentric_scoresHigh() {
        assertEquals(95f, Tempo.score(eccentricMs = 2000, concentricMs = 700, durationOk = true), 0.01f)
        assertEquals(95f, Tempo.score(eccentricMs = 1250, concentricMs = 1250, durationOk = true), 0.01f)
        assertFalse(Tempo.dropped(2000))
        assertFalse(Tempo.dropped(1250))
    }

    @Test
    fun dumpOnTheWayDown_scoresLowAndTagsDropped() {
        assertEquals(50f, Tempo.score(eccentricMs = 200, concentricMs = 1200, durationOk = true), 0.01f)
        assertEquals(50f, Tempo.score(eccentricMs = 400, concentricMs = 1200, durationOk = true), 0.01f)
        assertTrue(Tempo.dropped(200))
        assertTrue(Tempo.dropped(400))
        assertFalse(Tempo.dropped(800))
        assertEquals(85f, Tempo.score(eccentricMs = 800, concentricMs = 1200, durationOk = true), 0.01f)
        assertFalse(Tempo.dropped(800))
    }
}
