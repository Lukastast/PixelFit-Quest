package com.pixelfitquest.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pixelfitquest.R

const val TEMPLATES_ROUTE = "$WORKOUTS_HISTORY_SCREEN?tab=templates"
const val HISTORY_ROUTE = "$WORKOUTS_HISTORY_SCREEN?tab=history"

sealed class BottomNavItem(
    val route: String,
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unSelectedIcon: Int,
    @param:StringRes val labelRes: Int,
) {
    data object Home : BottomNavItem(HOME_SCREEN, R.drawable.homebuttonclicked, R.drawable.homebuttonunclicked, R.string.nav_home)
    data object Templates : BottomNavItem(TEMPLATES_ROUTE, R.drawable.templatesbuttonclicked, R.drawable.templatesbuttonunclicked, R.string.nav_templates)
    data object History : BottomNavItem(HISTORY_ROUTE, R.drawable.workoutbuttonclicked, R.drawable.workoutbuttonunclicked, R.string.nav_history)
    data object Health : BottomNavItem(HEALTH_CENTER_SCREEN, R.drawable.healthbuttonclicked, R.drawable.healthbuttonunclicked, R.string.nav_health)

    companion object {
        val items: List<BottomNavItem> = listOf(Home, Templates, History, Health)
    }
}