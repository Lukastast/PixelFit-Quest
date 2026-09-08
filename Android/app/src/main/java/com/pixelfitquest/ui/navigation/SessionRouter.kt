package com.pixelfitquest.ui.navigation

/**
 * First-launch routing. Auth is optional: splash never sends users to login.
 * Phone/Room is the source of truth regardless of Google sign-in.
 */
object SessionRouter {
    const val INTRO_SEEN_KEY = HAS_SEEN_INTRO

    fun startDestination(hasSeenIntro: Boolean): String {
        return if (hasSeenIntro) HOME_SCREEN else INTRO_SCREEN
    }
}
