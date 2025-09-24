package com.PixelFitQuest.ui.navigation

import com.PixelFitQuest.Helpers.CUSTOMIZATION_SCREEN
import com.PixelFitQuest.Helpers.HOME_SCREEN
import com.PixelFitQuest.Helpers.SETTINGS_SCREEN
import com.PixelFitQuest.Helpers.WORKOUT_SCREEN
import com.PixelFitQuest.R

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
        label = "Profile")
    object Customization : BottomNavItem(
        route = CUSTOMIZATION_SCREEN,
        selectedIcon = R.drawable.customizationbuttonclicked,
        unSelectedIcon = R.drawable.customizationbuttonunclicked,
        label = "Customization")
}