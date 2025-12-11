package com.pixelfitquest.helpers

import com.google.firebase.auth.FirebaseAuthException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AuthErrorMapperTest {

    @Before
    fun setUp() {
        mockkStatic("android.util.Log")
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `mapError returns generic message for non-Firebase exception`() {
        val exception = RuntimeException("Non-Firebase error")

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.signupErrorMappings)

        assertEquals("Non-Firebase error", result)
    }

    @Test
    fun `mapError returns fallback for non-Firebase exception with no message`() {
        val exception = RuntimeException()  // No message

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.signupErrorMappings)

        assertEquals("An error occurred", result)
    }

    @Test
    fun `mapError returns mapped message for ERROR_INVALID_EMAIL in login`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_INVALID_EMAIL"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings)

        assertEquals("Invalid email", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_INVALID_EMAIL") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_WRONG_PASSWORD in login`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_WRONG_PASSWORD"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings)

        assertEquals("Incorrect password", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_WRONG_PASSWORD") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_USER_NOT_FOUND in login`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_USER_NOT_FOUND"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings)

        assertEquals("No account found with this email", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_USER_NOT_FOUND") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_USER_DISABLED in login`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_USER_DISABLED"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings)

        assertEquals("Account disabled", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_USER_DISABLED") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_INVALID_CREDENTIAL in login`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_INVALID_CREDENTIAL"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings)

        assertEquals("Invalid email or password", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_INVALID_CREDENTIAL") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_INVALID_EMAIL in signup`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_INVALID_EMAIL"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.signupErrorMappings)

        assertEquals("Invalid email format", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_INVALID_EMAIL") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_EMAIL_ALREADY_IN_USE in signup`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.signupErrorMappings)

        assertEquals("Email already in use", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_EMAIL_ALREADY_IN_USE") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_WEAK_PASSWORD in signup`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_WEAK_PASSWORD"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.signupErrorMappings)

        assertEquals("Password is too weak", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_WEAK_PASSWORD") }
    }

    @Test
    fun `mapError returns mapped message for ERROR_INVALID_CREDENTIAL in signup`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_INVALID_CREDENTIAL"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.signupErrorMappings)

        assertEquals("Invalid credentials", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: ERROR_INVALID_CREDENTIAL") }
    }

    @Test
    fun `mapError returns exception message for unknown Firebase error code`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "UNKNOWN_CODE"
            every { message } returns "Custom error message"
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings)

        assertEquals("Custom error message", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: UNKNOWN_CODE") }
    }

    @Test
    fun `mapError returns default message when no exception message available`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "UNKNOWN_CODE"
            every { message } returns null
        }

        val result = AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings, defaultMessage = "Fallback default")

        assertEquals("Fallback default", result)
        verify { android.util.Log.e(ERROR_TAG, "Firebase error code: UNKNOWN_CODE") }
    }

    @Test
    fun `mapError uses custom error tag`() {
        val exception = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_WRONG_PASSWORD"
        }

        AuthErrorMapper.mapError(exception, AuthErrorMapper.loginErrorMappings, errorTag = "CUSTOM_TAG")

        verify { android.util.Log.e("CUSTOM_TAG", "Firebase error code: ERROR_WRONG_PASSWORD") }
    }
}