package com.PixelFitQuest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.PixelFitQuest.Helpers.SIGNUP_SCREEN
import com.PixelFitQuest.R
import com.PixelFitQuest.ext.AuthenticationButton
import com.PixelFitQuest.ext.launchCredManBottomSheet
import com.PixelFitQuest.ui.theme.PixelFitQuestTheme
import com.PixelFitQuest.ui.theme.typography
import com.PixelFitQuest.viewmodel.AuthState
import com.PixelFitQuest.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    openScreen: (String) -> Unit,
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    val authState = viewModel.authState.collectAsState().value

    LaunchedEffect(Unit) {
        launchCredManBottomSheet(context) { credential ->
            viewModel.onLogInWithGoogle(credential, openAndPopUp)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background PNG image
        Image(
            painter = painterResource(R.drawable.logsigninbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Foreground board PNG image
        Image(
            painter = painterResource(R.drawable.questloginboard),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.95f)
                .aspectRatio(1f)
                .padding(8.dp)
                .align(Alignment.Center)
                .scale(scaleX = 1.2f, scaleY = 1.5f),
            contentScale = ContentScale.FillBounds
        )

        // Foreground login content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 70.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Email:",
                style = typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.inputfield),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                TextField(
                    singleLine = true,
                    value = email.value,
                    onValueChange = { viewModel.updateEmail(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.96f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
            }

            Text(
                text = "Password:",
                style = typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.inputfield),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                TextField(
                    singleLine = true,
                    value = password.value,
                    onValueChange = { viewModel.updatePassword(it) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.96f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (authState is AuthState.Error) {
                Text(
                    text = authState.message,
                    style = typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            TextButton(onClick = { openScreen(SIGNUP_SCREEN) }) {
                Text(text = stringResource(R.string.sign_up_description), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.onLogInClick(openAndPopUp) },
                    modifier = Modifier.weight(1f),
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(
                        text = stringResource(R.string.log_in),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                AuthenticationButton(
                    buttonText = R.string.login_with_google,
                    modifier = Modifier.weight(1f)
                ) { credential ->
                    viewModel.onLogInWithGoogle(credential, openAndPopUp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
            openScreen = {},
            openAndPopUp = { _, _ -> }
        )
    }
}