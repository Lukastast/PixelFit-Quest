package com.pixelfitquest.ext

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.pixelfitquest.helpers.ERROR_TAG
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.pixelfitquest.ui.components.PixelArtButton
import kotlinx.coroutines.launch

@Composable
fun AuthenticationButton(
    buttonText: Int,
    modifier: Modifier = Modifier,
    onRequestResult: (Credential) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    PixelArtButton(
        onClick = {
            coroutineScope.launch {
                launchCredManButtonUI(
                    context = context,
                    onRequestResult = onRequestResult
                )
            }
        },
        imageRes = R.drawable.button_signup_unclicked,
        pressedRes = R.drawable.button_clicked,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google_g),
                modifier = Modifier.padding(horizontal = 8.dp),
                contentDescription = "Google logo"
            )
            Text(
                text = stringResource(buttonText),
                fontSize = 16.sp
            )
        }
    }
}

suspend fun launchCredManButtonUI(
    context: Context,
    credentialManager: CredentialManager = CredentialManager.create(context),
    onRequestResult: (Credential) -> Unit
) {
    try {
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        onRequestResult(result.credential)
    } catch (e: NoCredentialException) {
        Log.d(ERROR_TAG, e.message.orEmpty())
        SnackbarManager.showMessage(context.getString(R.string.no_accounts_error))
    } catch (e: GetCredentialException) {
        e.printStackTrace()
        Log.d(ERROR_TAG, e.message.orEmpty())
    }
}

suspend fun launchCredManBottomSheet(
    context: Context,
    hasFilter: Boolean = true,
    credentialManager: CredentialManager = CredentialManager.create(context),
    onRequestResult: (Credential) -> Unit
) {
    try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(hasFilter)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        onRequestResult(result.credential)
    } catch (e: NoCredentialException) {
        Log.d(ERROR_TAG, e.message.orEmpty())
        if (hasFilter) {
            launchCredManBottomSheet(
                context = context,
                hasFilter = false,
                credentialManager = credentialManager,
                onRequestResult = onRequestResult
            )
        }
    } catch (e: GetCredentialException) {
        Log.d(ERROR_TAG, e.message.orEmpty())
    }
}