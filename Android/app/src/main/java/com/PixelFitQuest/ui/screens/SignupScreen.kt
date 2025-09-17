package com.PixelFitQuest.ui.screens

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PixelFitQuest.R
import com.PixelFitQuest.model.auth
import com.PixelFitQuest.ui.theme.PixelFitQuestTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val db = Firebase.firestore
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.logsigninbackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // show success message
        successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // show errormessage
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                val trimmedEmail = email.trim()
                val trimmedUsername = username.trim()
                Log.d("SignUpScreen", "Email after trim: '$trimmedEmail'")
                Log.d("SignUpScreen", "Username after trim: '$trimmedUsername'")
                when {
                    trimmedUsername.isBlank() -> {
                        errorMessage = "Please fill out username"
                    }
                    trimmedEmail.isBlank() -> {
                        errorMessage = "Please fill out email"
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                        errorMessage = "please use a valid email"
                    }
                    password.isBlank() -> {
                        errorMessage = "Please fill out password"
                    }
                    password.length < 6 -> {
                        errorMessage = "Password must be 6 characters"
                    }
                    else -> {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        auth.createUserWithEmailAndPassword(trimmedEmail, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    successMessage = "User successfully created!"
                                    val user = auth.currentUser
                                    val userData = hashMapOf(
                                        "username" to trimmedUsername,
                                        "email" to trimmedEmail
                                    )
                                    user?.let {
                                        db.collection("users").document(it.uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Log.d("SignUpScreen", "Userdata saved in Firestore for UID: ${user.uid}")
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Could not save userdata: ${e.message}"
                                                Log.e("SignUpScreen", "Error when saving to firestore: ${e.message}")
                                            }
                                    } ?: run {
                                        errorMessage = "User signup failed: could not get userdata"
                                    }
                                    // Navigate to home screen after successful signup
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = task.exception?.message ?: "Signup failed"
                                    Log.e("SignUpScreen", "Signup failed: ${task.exception?.message}")
                                }
                                isLoading = false
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun SignUpScreenPreview() {
    PixelFitQuestTheme {
        SignUpScreen(navController = rememberNavController())
        }
    }
}
