package com.pixelfitquest.ext

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringValidationTest {

    @Test
    fun `isValidEmail returns true for valid emails`() {
        assertTrue("test@example.com".isValidEmail())
        assertTrue("user.name+tag@gmail.com".isValidEmail())
        assertTrue("valid-email@sub.domain.co.uk".isValidEmail())
        assertTrue("1234567890@example.com".isValidEmail())
    }

    @Test
    fun `isValidEmail returns false for invalid or blank emails`() {
        assertFalse("".isValidEmail())
        assertFalse("   ".isValidEmail())
        assertFalse("invalid".isValidEmail())
        assertFalse("invalid@".isValidEmail())
        assertFalse("invalid@domain".isValidEmail())
        assertFalse("user@.com".isValidEmail())
        assertFalse("user@domain..com".isValidEmail())
        assertFalse("user@ domain.com".isValidEmail())
        assertFalse("@domain.com".isValidEmail())
    }

    @Test
    fun `isValidPassword returns true for valid passwords`() {
        assertTrue("Passw0rd".isValidPassword()) // Length 8, has upper, lower, digit
        assertTrue("Ab1def".isValidPassword()) // Minimum length 6, has all required
        assertTrue("Secure1Pass!".isValidPassword()) // Allows special chars
        assertTrue("A1b2c3".isValidPassword())
        assertTrue("Abcde1".isValidPassword()) // Valid: Length 6, upper, lower, digit
    }

    @Test
    fun `isValidPassword returns false for invalid passwords`() {
        assertFalse("".isValidPassword()) // Blank
        assertFalse("   ".isValidPassword()) // Blank spaces
        assertFalse("short".isValidPassword()) // Length 5 < 6, no digit/upper
        assertFalse("P1a".isValidPassword()) // Length 3 < 6
        assertFalse("password1".isValidPassword()) // No upper
        assertFalse("PASSWORD1".isValidPassword()) // No lower
        assertFalse("Passwrd".isValidPassword()) // No digit
        assertFalse("Pass w0rd".isValidPassword()) // Has space
        assertFalse("Ab1de".isValidPassword()) // Length 5 < 6
        assertFalse("A b1def".isValidPassword()) // Has space
    }
}