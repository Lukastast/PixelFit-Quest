package com.PixelFitQuest.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.PixelFitQuest.R
import com.PixelFitQuest.ext.AuthenticationButton
import com.PixelFitQuest.Helpers.SIGNUP_SCREEN
import com.PixelFitQuest.ui.theme.typography
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

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.logsigninbackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", style = typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(48.dp))
            OutlinedTextField(
                value = email.value,
                onValueChange = { viewModel.updateEmail(it) },
                placeholder = { Text(stringResource(R.string.email), style = typography.labelLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { viewModel.updatePassword (it) },
                label = {Text(stringResource(R.string.password) , style = typography.labelLarge) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { openScreen(SIGNUP_SCREEN) }) {
                Text(text = stringResource(R.string.sign_up_description), fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onLogInClick(openAndPopUp) },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    fontSize = 16.sp,
                    modifier = modifier.padding(0.dp, 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            AuthenticationButton(buttonText = R.string.login_with_google) { credential ->
                            viewModel.onLogInWithGoogle(credential, openAndPopUp)
                        }
        }
    }
}