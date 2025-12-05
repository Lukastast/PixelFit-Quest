package com.pixelfitquest.ui.view

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
import com.pixelfitquest.R
import com.pixelfitquest.ext.AuthenticationButton
import com.pixelfitquest.ext.launchCredManBottomSheet
import com.pixelfitquest.helpers.AutoSizeText
import com.pixelfitquest.helpers.LOGIN_SCREEN
import com.pixelfitquest.model.service.AuthState
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.SignupViewModel

@Composable
fun SignupScreen(
    openScreen: (String) -> Unit,
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    val confirmPassword = viewModel.confirmPassword.collectAsState()
    val authState = viewModel.authState.collectAsState().value

    LaunchedEffect(Unit) {
        launchCredManBottomSheet(context) { credential ->
            viewModel.onSignUpWithGoogle(credential, openAndPopUp)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.logsigninbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 70.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoSizeText(
                text = "Sign Up",
                style = typography.titleLarge.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 8.dp),
                maxFontSize = 32.sp,
                minFontSize = 30.sp
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
                    .height(60.dp),
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
                        errorIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
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
                    .height(60.dp),
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
                        errorIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            Text(
                text = "Confirm Password:",
                style = typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(60.dp),
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
                    value = confirmPassword.value,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
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
                        errorIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
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

            TextButton(onClick = { openScreen(LOGIN_SCREEN) }) {
                Text(text = stringResource(R.string.login_description), fontSize = 14.sp, color=Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PixelArtButton(
                    onClick = { viewModel.onSignUpClick(openAndPopUp) },
                    imageRes = R.drawable.button_signup_unclicked,
                    pressedRes = R.drawable.button_signup_clicked,
                    modifier = Modifier.weight(1f).height(60.dp)
                ) {
                    Text("Sign Up")
                }

                Spacer(modifier = Modifier.width(8.dp))

                AuthenticationButton(
                    buttonText = R.string.sign_up_with_google,
                    modifier = Modifier.weight(1f).height(59.dp)
                ) { credential ->
                    viewModel.onSignUpWithGoogle(credential, openAndPopUp)
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
fun SignupScreenPreview() {
    PixelFitQuestTheme {
        SignupScreen(
            openScreen = {},
            openAndPopUp = { _, _ -> }
        )
    }
}