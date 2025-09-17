package com.PixelFitQuest.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PixelFitQuest.R
import com.PixelFitQuest.ui.theme.PixelFitQuestTheme
import com.PixelFitQuest.ui.theme.typography
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

fun Modifier.draw9Patch(drawable: Drawable) = drawBehind {
    drawIntoCanvas { canvas ->
        drawable.bounds = android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
        drawable.draw(canvas.nativeCanvas)
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    onSignIn: suspend (String, String) -> Result<FirebaseUser?>
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.logsigninbackground)!!
    val boardDrawable = ContextCompat.getDrawable(context, R.drawable.questloginboard)!!

    Box(modifier = Modifier.fillMaxSize()) {
        // Background 9-patch image
        Box(
            modifier = Modifier
                .matchParentSize()
                .draw9Patch(backgroundDrawable)
        )

        // Foreground 9-patch image
        Box(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .aspectRatio(1f)
                .padding(8.dp)
                .align(Alignment.Center)
                .scale(1.2f)
                .draw9Patch(boardDrawable)
        )

        // Foreground login content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", style = typography.labelLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", style = typography.labelLarge) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            error?.let {
                Text(
                    text = it,
                    style = typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        scope.launch {
                            val result = onSignIn(email, password)
                            if (result.isSuccess) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                error = result.exceptionOrNull()?.message ?: "Login failed"
                            }
                        }
                    } else {
                        error = "Fill all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login", style = typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign up", style = typography.labelLarge)
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "id:pixel_5",
    showSystemUi = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun LoginScreenPreview() {
    PixelFitQuestTheme {
        LoginScreen(
            navController = rememberNavController(),
            onSignIn = { email, password ->
                if (email.isNotBlank() && password.isNotBlank()) {
                    Result.success(null) // Simulate successful login
                } else {
                    Result.failure(Exception("Fill all fields"))
                }
            }
        )
    }
}