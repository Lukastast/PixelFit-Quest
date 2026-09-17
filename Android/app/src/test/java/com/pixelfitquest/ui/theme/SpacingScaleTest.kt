package com.pixelfitquest.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacingScaleTest {

    @Test
    fun compactDesignPhone_isBaselineScale() {
        val scale = computeSpacingScale(widthDp = 360, heightDp = 800)
        assertEquals(1.0f, scale, 0.001f)
        assertEquals(PixelFitWidthClass.Compact, PixelFitWidthClass.fromWidthDp(360))
    }

    @Test
    fun smallPhone_tightensSpacing() {
        val scale = computeSpacingScale(widthDp = 320, heightDp = 568)
        assertTrue("expected tighter than baseline, was $scale", scale < 1.0f)
        assertTrue("expected >= 0.88, was $scale", scale >= 0.88f)
    }

    @Test
    fun largePhone_growsSpacingWithinCompactCap() {
        val scale = computeSpacingScale(widthDp = 430, heightDp = 930)
        assertTrue("expected looser than baseline, was $scale", scale > 1.0f)
        assertTrue("expected <= 1.18 compact cap, was $scale", scale <= 1.18f)
        assertEquals(PixelFitWidthClass.Compact, PixelFitWidthClass.fromWidthDp(430))
    }

    @Test
    fun landscapePhone_doesNotJumpToTabletScale() {
        val portrait = computeSpacingScale(widthDp = 360, heightDp = 800)
        val landscape = computeSpacingScale(widthDp = 800, heightDp = 360)
        assertEquals(portrait, landscape, 0.001f)
        assertEquals(PixelFitWidthClass.Medium, PixelFitWidthClass.fromWidthDp(800))
    }

    @Test
    fun mediumWindow_usesMediumScale() {
        val scale = computeSpacingScale(widthDp = 700, heightDp = 1000)
        assertEquals(1.28f, scale, 0.001f)
        assertEquals(PixelFitWidthClass.Medium, PixelFitWidthClass.fromWidthDp(700))
    }

    @Test
    fun expandedTablet_usesExpandedScale() {
        val scale = computeSpacingScale(widthDp = 900, heightDp = 1200)
        assertEquals(1.45f, scale, 0.001f)
        assertEquals(PixelFitWidthClass.Expanded, PixelFitWidthClass.fromWidthDp(900))
    }

    @Test
    fun fromWindow_scalesTokensFromEightPointGrid() {
        val compact = PixelFitSpacing.fromWindow(360, 800)
        assertEquals(8f, compact.xs.value, 0.01f)
        assertEquals(16f, compact.md.value, 0.01f)
        assertEquals(32f, compact.xl.value, 0.01f)

        val small = PixelFitSpacing.fromWindow(320, 568)
        assertTrue(small.md.value < compact.md.value)
        assertTrue(small.screen.value < compact.screen.value)
    }
}
