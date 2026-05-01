package com.michael.insightlyspend.presentation.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.michael.insightlyspend.R
import androidx.fragment.app.FragmentActivity

@Composable
fun BiometricGate(
    activity: FragmentActivity,
    content: @Composable () -> Unit,
) {
    val ctx = LocalContext.current
    var unlocked by remember { mutableStateOf(false) }

    fun authenticate() {
        val manager = BiometricManager.from(ctx)
        val status = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            unlocked = true
            return
        }
        val executor = ContextCompat.getMainExecutor(ctx)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    unlocked = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    unlocked = false
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(ctx.getString(R.string.biometric_prompt_title))
            .setSubtitle(ctx.getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(ctx.getString(R.string.cancel))
            .build()
        prompt.authenticate(info)
    }

    if (unlocked) {
        content()
    } else {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.authenticate_title), style = MaterialTheme.typography.titleMedium)
            Button(onClick = { authenticate() }) {
                Text(stringResource(R.string.unlock_biometrics))
            }
        }
    }
}
