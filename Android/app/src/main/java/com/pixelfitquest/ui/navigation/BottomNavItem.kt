package com.pixelfitquest.ui.navigation

import com.pixelfitquest.Helpers.CUSTOMIZATION_SCREEN
import com.pixelfitquest.Helpers.HOME_SCREEN
import com.pixelfitquest.Helpers.SETTINGS_SCREEN
import com.pixelfitquest.Helpers.WORKOUT_SCREEN
import com.pixelfitquest.R

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: Int,
    val unSelectedIcon: Int,
    val label: String) {
    object Home : BottomNavItem(
        route = HOME_SCREEN,
        selectedIcon = R.drawable.homebuttonclicked,
        unSelectedIcon = R.drawable.homebuttonunclicked,
        label = "Home")
    object Settings : BottomNavItem(
        route = SETTINGS_SCREEN,
        selectedIcon = R.drawable.settingsbuttonclicked,
        unSelectedIcon = R.drawable.settingsbuttonunclicked,
        label = "Settings")
    object Workout : BottomNavItem(
        route = WORKOUT_SCREEN,
        selectedIcon = R.drawable.workoutbuttonclicked,
        unSelectedIcon = R.drawable.workoutbuttonunclicked,
        label = "Workout")
    object Customization : BottomNavItem(
        route = CUSTOMIZATION_SCREEN,
        selectedIcon = R.drawable.customizationbuttonclicked,
        unSelectedIcon = R.drawable.customizationbuttonunclicked,
        label = "Customization")
}