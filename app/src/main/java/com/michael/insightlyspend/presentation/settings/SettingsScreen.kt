package com.michael.insightlyspend.presentation.settings

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.domain.repository.AppLanguage
import com.michael.insightlyspend.domain.repository.ThemePreference
import kotlinx.coroutines.launch

private val COMMON_CURRENCIES = listOf("USD", "EUR", "GBP", "AED", "SAR", "EGP", "JPY", "INR")

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val currency by vm.currencyCode.collectAsState()
    val language by vm.appLanguage.collectAsState()
    val themePref by vm.themePreference.collectAsState()
    val biometric by vm.biometricRequired.collectAsState()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val mainScroll = rememberScrollState()
    val currencyScroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(mainScroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)

        CategoriesManageSection()

        RecurringRulesSection()

        Text(stringResource(R.string.settings_currency))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(currencyScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            COMMON_CURRENCIES.forEach { code ->
                FilterChip(
                    selected = currency == code,
                    onClick = { vm.setCurrency(code) },
                    label = { Text(code) },
                )
            }
        }

        Text(stringResource(R.string.settings_language))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = language == AppLanguage.SYSTEM,
                onClick = { vm.setAppLanguage(AppLanguage.SYSTEM) },
                label = { Text(stringResource(R.string.settings_language_system)) },
            )
            FilterChip(
                selected = language == AppLanguage.ENGLISH,
                onClick = { vm.setAppLanguage(AppLanguage.ENGLISH) },
                label = { Text(stringResource(R.string.settings_language_en)) },
            )
            FilterChip(
                selected = language == AppLanguage.ARABIC,
                onClick = { vm.setAppLanguage(AppLanguage.ARABIC) },
                label = { Text(stringResource(R.string.settings_language_ar)) },
            )
        }

        Text(stringResource(R.string.settings_theme))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = themePref == ThemePreference.SYSTEM,
                onClick = { vm.setTheme(ThemePreference.SYSTEM) },
                label = { Text(stringResource(R.string.theme_system)) },
            )
            FilterChip(
                selected = themePref == ThemePreference.LIGHT,
                onClick = { vm.setTheme(ThemePreference.LIGHT) },
                label = { Text(stringResource(R.string.theme_light)) },
            )
            FilterChip(
                selected = themePref == ThemePreference.DARK,
                onClick = { vm.setTheme(ThemePreference.DARK) },
                label = { Text(stringResource(R.string.theme_dark)) },
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.settings_biometric))
            Switch(checked = biometric, onCheckedChange = vm::setBiometric)
        }

        Text(
            stringResource(R.string.settings_export_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = {
                scope.launch {
                    val uri = vm.exportCsv()
                    shareUri(ctx, uri, "text/csv")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.export_csv)) }

        Button(
            onClick = {
                scope.launch {
                    val uri = vm.exportPdf()
                    shareUri(ctx, uri, "application/pdf")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.export_pdf)) }
    }
}

private fun shareUri(context: android.content.Context, uri: android.net.Uri, type: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        this.type = type
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_export)))
}
