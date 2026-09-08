package com.pixelfitquest.helpers

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun resetPasswordMappings_coverExpectedFirebaseCodes() {
        val mappings = AuthErrorMapper.resetPasswordErrorMappings
        assertEquals("Invalid email", mappings["ERROR_INVALID_EMAIL"])
        assertEquals("No account found with this email", mappings["ERROR_USER_NOT_FOUND"])
        assertEquals("Account disabled", mappings["ERROR_USER_DISABLED"])
        assertEquals("Too many attempts. Try again later", mappings["ERROR_TOO_MANY_REQUESTS"])
    }
}
