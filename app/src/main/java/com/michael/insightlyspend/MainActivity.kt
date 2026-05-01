package com.michael.insightlyspend

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.michael.insightlyspend.domain.repository.AppLanguage
import com.michael.insightlyspend.domain.repository.ThemePreference
import com.michael.insightlyspend.presentation.navigation.MainShell
import com.michael.insightlyspend.presentation.root.MainThemeViewModel
import com.michael.insightlyspend.presentation.security.BiometricGate
import com.michael.insightlyspend.ui.theme.InsightlySpendTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeVm: MainThemeViewModel = viewModel()
            val appLanguage by themeVm.appLanguage.collectAsState()
            LaunchedEffect(appLanguage) {
                val locales = when (appLanguage) {
                    AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                    AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
                    AppLanguage.ARABIC -> LocaleListCompat.forLanguageTags("ar")
                }
                AppCompatDelegate.setApplicationLocales(locales)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { }
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val themePref by themeVm.themePreference.collectAsState()
            val biometric by themeVm.biometricRequired.collectAsState()
            val darkTheme = when (themePref) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            InsightlySpendTheme(darkTheme = darkTheme, dynamicColor = true) {
                if (biometric) {
                    BiometricGate(activity = this) {
                        MainShell()
                    }
                } else {
                    MainShell()
                }
            }
        }
    }
}
