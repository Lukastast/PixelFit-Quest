package com.PixelFitQuest.ui.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.PixelFitQuest.ui.screens.CustomizationScreen
import com.PixelFitQuest.ui.screens.HomeScreen
import com.PixelFitQuest.ui.screens.LoginScreen
import com.PixelFitQuest.ui.screens.SettingsScreen
import com.PixelFitQuest.ui.screens.SignUpScreen
//import com.PixelFitQuest.ui.screens.SignUpScreen
import com.PixelFitQuest.ui.screens.WorkoutScreen
import com.PixelFitQuest.ui.theme.PixelFitQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelFitQuestTheme {
                PixelFitQuestApp()
                }
            }
        }
    }

@Composable
fun PixelFitQuestApp() {
    val navController = rememberNavController()
    val startDestination = "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }

        composable("home") {
            AppScaffold(navController = navController, currentRoute = "home") {
                HomeScreen(navController = navController)
            }
        }
        composable("settings") {
                    AppScaffold(navController = navController, currentRoute = "settings") {
                        SettingsScreen(navController = navController)
                    }
                }
                composable("workout") {
                    AppScaffold(navController = navController, currentRoute = "workout") {
                        WorkoutScreen(navController = navController)
                    }
                }
                composable("customization") {
                    AppScaffold(navController = navController, currentRoute = "customization") {
                        CustomizationScreen(navController = navController)
                    }
                }

    }
}
