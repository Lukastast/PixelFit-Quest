package com.pixelfitquest.local

import com.pixelfitquest.ui.navigation.HOME_SCREEN
import com.pixelfitquest.ui.navigation.INTRO_SCREEN
import com.pixelfitquest.ui.navigation.SessionRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SessionRouterTest {
    @Test
    fun firstLaunchGoesToIntroNotLogin() {
        val route = SessionRouter.startDestination(hasSeenIntro = false)
        assertEquals(INTRO_SCREEN, route)
        assertNotEquals("login", route)
        assertNotEquals("signup", route)
    }

    @Test
    fun returningLaunchGoesHomeNotAuth() {
        val route = SessionRouter.startDestination(hasSeenIntro = true)
        assertEquals(HOME_SCREEN, route)
        assertNotEquals("login", route)
        assertNotEquals("signup", route)
    }
}
