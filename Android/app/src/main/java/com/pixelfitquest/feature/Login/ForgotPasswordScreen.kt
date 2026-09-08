package com.pixelfitquest.feature.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.firebase.service.AuthState
import com.pixelfitquest.helpers.AutoSizeText
import com.pixelfitquest.ui.navigation.FORGOT_PASSWORD_SCREEN
import com.pixelfitquest.ui.navigation.LOGIN_SCREEN
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.typography

@Composable
fun ForgotPasswordScreen(
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val email = viewModel.email.collectAsState()
    val authState = viewModel.authState.collectAsState().value

    Box(modifier = modifier.fillMaxSize()) {
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
                text = stringResource(R.string.forgot_password_title),
                style = typography.titleLarge.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 8.dp),
                maxFontSize = 32.sp,
                minFontSize = 22.sp
            )

            Text(
                text = stringResource(R.string.forgot_password_instructions),
                style = typography.labelLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = stringResource(R.string.email_label),
                style = typography.labelLarge,
                modifier = Modifier.align(Alignment.Start)
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
                    enabled = authState !is AuthState.Loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.onSendResetClick() }
                    ),
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
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (authState is AuthState.Error) {
                Text(
                    text = authState.message,
                    style = typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (authState is AuthState.Success) {
                Text(
                    text = stringResource(R.string.forgot_password_success),
                    style = typography.labelLarge,
                    color = VitalGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            TextButton(
                onClick = { openAndPopUp(LOGIN_SCREEN, FORGOT_PASSWORD_SCREEN) }
            ) {
                Text(
                    text = stringResource(R.string.back_to_login),
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                PixelArtButton(
                    onClick = { viewModel.onSendResetClick() },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp)
                ) {
                    Text(
                        text = stringResource(R.string.send_reset),
                        fontSize = 16.sp
                    )
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
fun ForgotPasswordScreenPreview() {
    PixelFitQuestTheme {
        ForgotPasswordScreen(
            openAndPopUp = { _, _ -> }
        )
    }
}
