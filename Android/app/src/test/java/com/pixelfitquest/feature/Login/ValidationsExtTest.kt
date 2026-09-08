package com.pixelfitquest.feature.Login

import com.pixelfitquest.feature.Login.ext.isValidEmail
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationsExtTest {

    @Test
    fun validEmail_acceptsStandardAddress() {
        assertTrue("hero@pixelfit.quest".isValidEmail())
    }

    @Test
    fun validEmail_rejectsBlankAndMalformed() {
        assertFalse("".isValidEmail())
        assertFalse("   ".isValidEmail())
        assertFalse("not-an-email".isValidEmail())
        assertFalse("hero@".isValidEmail())
    }
}
