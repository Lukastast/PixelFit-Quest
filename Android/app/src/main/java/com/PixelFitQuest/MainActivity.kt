package com.PixelFitQuest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.PixelFitQuest.ui.navigation.AppScaffold
import com.PixelFitQuest.ui.theme.PixelFitQuestTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            PixelFitQuestTheme {
                    AppScaffold()
            }
        }
        }
    }