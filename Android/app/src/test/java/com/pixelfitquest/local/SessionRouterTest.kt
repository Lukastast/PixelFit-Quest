package com.pixelfitquest.local

import com.pixelfitquest.ui.navigation.HOME_SCREEN
import com.pixelfitquest.ui.navigation.INTRO_SCREEN
import com.pixelfitquest.ui.navigation.LOGIN_SCREEN
import com.pixelfitquest.ui.navigation.SessionRouter
import com.pixelfitquest.ui.navigation.SIGNUP_SCREEN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SessionRouterTest {
    @Test
    fun firstLaunchGoesToIntroNotLogin() {
        val route = SessionRouter.startDestination(hasSeenIntro = false)
        assertEquals(INTRO_SCREEN, route)
        assertNotEquals(LOGIN_SCREEN, route)
        assertNotEquals(SIGNUP_SCREEN, route)
    }

    @Test
    fun returningLaunchGoesHomeNotAuth() {
        val route = SessionRouter.startDestination(hasSeenIntro = true)
        assertEquals(HOME_SCREEN, route)
        assertNotEquals(LOGIN_SCREEN, route)
        assertNotEquals(SIGNUP_SCREEN, route)
    }
}
