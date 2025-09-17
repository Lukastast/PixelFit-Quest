package com.PixelFitQuest.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.receiptsaver.ui.navigation.NavigationBar

@Composable
fun AppScaffold(
    navController: NavController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        bottomBar = {
            NavigationBar(navController = navController, currentRoute = currentRoute)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}