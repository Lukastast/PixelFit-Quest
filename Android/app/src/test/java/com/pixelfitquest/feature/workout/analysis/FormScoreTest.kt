package com.pixelfitquest.feature.workout.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormScoreTest {

    @Test
    fun shortRomHurtsMoreThanADump() {
        val shortRom = formScoreFrom(romScore = 60f, tempoScore = 95f, barQuality = 100f)
        val dumped = formScoreFrom(romScore = 100f, tempoScore = 50f, barQuality = 100f)
        assertTrue("short ROM $shortRom should score below a dump $dumped", shortRom < dumped)
    }

    @Test
    fun weightsAreRomThenBarThenTempo() {
        // 50/30/20 of 60, 100, 95
        assertEquals(79f, formScoreFrom(romScore = 60f, tempoScore = 95f, barQuality = 100f), 0.01f)
        // missing ROM renormalizes bar 30 and tempo 20
        val barAndTempo = formScoreFrom(romScore = null, tempoScore = 50f, barQuality = 100f)
        assertEquals((100f * 0.30f + 50f * 0.20f) / 0.50f, barAndTempo, 0.01f)
    }
}
