package com.pixelfitquest.ui.navigation

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

    object Workouts : BottomNavItem(
        route = WORKOUTS_HISTORY_SCREEN,
        selectedIcon = R.drawable.workoutbuttonclicked,
        unSelectedIcon = R.drawable.workoutbuttonunclicked,
        label = "Workouts")

    object HealthCenter : BottomNavItem(
        route = HEALTH_CENTER_SCREEN,
        selectedIcon = R.drawable.healthbuttonclicked,
        unSelectedIcon = R.drawable.healthbuttonunclicked,
        label = "Quests")

    object Customization : BottomNavItem(
        route = CUSTOMIZATION_SCREEN,
        selectedIcon = R.drawable.customizationbuttonclicked,
        unSelectedIcon = R.drawable.customizationbuttonunclicked,
        label = "Customization")

    object Settings : BottomNavItem(
        route = SETTINGS_SCREEN,
        selectedIcon = R.drawable.settingsbuttonclicked,
        unSelectedIcon = R.drawable.settingsbuttonunclicked,
        label = "Settings")

    object WorkoutCustomization : BottomNavItem(
        route = WORKOUT_CUSTOMIZATION_SCREEN,
        selectedIcon = R.drawable.workoutbuttonclicked,
        unSelectedIcon = R.drawable.workoutbuttonunclicked,
        label = "workout_customization")
}