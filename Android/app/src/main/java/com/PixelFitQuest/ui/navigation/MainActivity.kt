package com.PixelFitQuest.ui.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.PixelFitQuest.ui.navigation.AppScaffold
import com.PixelFitQuest.ui.screens.CustomizationScreen
import com.PixelFitQuest.ui.screens.HomeScreen
import com.PixelFitQuest.ui.screens.LoginScreen
import com.PixelFitQuest.ui.screens.SettingsScreen
import com.PixelFitQuest.ui.screens.SignupScreen
import com.PixelFitQuest.ui.screens.WorkoutScreen
import com.PixelFitQuest.ui.theme.PixelFitQuestTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            PixelFitQuestTheme {
                AppNavigation(auth)
                }
            }
        }
    }

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val startDestination = if (auth.currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onSignIn = { email, password ->
                    try {
                        val result = auth.signInWithEmailAndPassword(email, password).await()
                        Result.success(result.user)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
            )
        }
        composable("signup") {
            SignupScreen(
                navController = navController,
                onSignUp = { email, password ->
                    try {
                        val result = auth.createUserWithEmailAndPassword(email, password).await()
                        result.user?.sendEmailVerification()?.await()
                        Result.success(result.user)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
            )
        }
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