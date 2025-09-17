package com.example.receiptsaver.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.PixelFitQuest.R

@Composable
fun NavigationBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        modifier = Modifier
            .paint(
                painter = painterResource(id = R.drawable.navbar),
                contentScale = ContentScale.Crop,
                ),
        containerColor = Color.Transparent,
    ) {
    NavigationBarItem(
            icon = {
                if (currentRoute == "home") {
                    Icon(
                        painter = painterResource(id = R.drawable.homebuttonclicked),
                        contentDescription = "HomeClicked",
                        tint = Color.Unspecified,
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.homebuttonunclicked),
                        contentDescription = "HomeUnclicked",
                        tint = Color.Unspecified
                    )
                } },
            selected = currentRoute == "home",
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,),
            onClick = { navigateToHome(navController) }
        )
        NavigationBarItem(
            icon = {
                if (currentRoute == "settings") {
                    Icon(
                        painter = painterResource(id = R.drawable.settingsbuttonclicked),
                        contentDescription = "SettingsClicked",
                        tint = Color.Unspecified
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.settingsbuttonunclicked),
                        contentDescription = "SettingsUnclicked",
                        tint = Color.Unspecified
                    )
                } },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,),
            selected = currentRoute == "settings",
            onClick = { navigateToSettings(navController) }
        )
        NavigationBarItem(
            icon = {
                if (currentRoute == "customization") {
                    Icon(
                        painter = painterResource(id = R.drawable.customizationbuttonclicked),
                        contentDescription = "CustomizationClicked",
                        tint = Color.Unspecified
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.customizationbuttonunclicked),
                        contentDescription = "CustomizationUnclicked",
                        tint = Color.Unspecified
                    )
                } },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,),
            selected = currentRoute == "customization",
            onClick = { navigateToCustomization(navController) }
                )
        NavigationBarItem(
            icon = {
                if (currentRoute == "workout") {
                    Icon(
                        painter = painterResource(id = R.drawable.workoutbuttonclicked),
                        contentDescription = "WorkoutClicked",
                        tint = Color.Unspecified
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.workoutbuttonunclicked),
                        contentDescription = "WorkoutUnclicked",
                        tint = Color.Unspecified
                    )
                } },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,),
            selected = currentRoute == "workout",
            onClick = { navigateToWorkout(navController) }
                )
    }
}

fun navigateToCustomization(navController: NavController){
    navController.navigate("Customization"){
        popUpTo(navController.graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}

fun navigateToHome(navController: NavController) {
    navController.navigate("home") {
        popUpTo(navController.graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}

fun navigateToSettings(navController: NavController) {
    navController.navigate("Settings") {
        popUpTo(navController.graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}

fun navigateToWorkout(navController: NavController) {
    navController.navigate("workout") {
        popUpTo(navController.graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}