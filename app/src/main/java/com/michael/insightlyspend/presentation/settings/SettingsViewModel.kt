package com.michael.insightlyspend.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.data.export.ReportExporter
import com.michael.insightlyspend.domain.repository.AppLanguage
import com.michael.insightlyspend.domain.repository.ThemePreference
import com.michael.insightlyspend.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val exporter: ReportExporter,
) : ViewModel() {

    val currencyCode: StateFlow<String> =
        prefs.currencyCode.stateIn(viewModelScope, SharingStarted.Eagerly, "USD")

    val themePreference: StateFlow<ThemePreference> =
        prefs.themePreference.stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreference.SYSTEM)

    val biometricRequired: StateFlow<Boolean> =
        prefs.biometricRequired.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appLanguage: StateFlow<AppLanguage> =
        prefs.appLanguage.stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.SYSTEM)

    fun setCurrency(code: String) {
        viewModelScope.launch { prefs.setCurrencyCode(code) }
    }

    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch { prefs.setThemePreference(theme) }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometricRequired(enabled) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { prefs.setAppLanguage(language) }
    }

    suspend fun exportCsv() = exporter.exportCsv()

    suspend fun exportPdf(): android.net.Uri =
        exporter.exportPdfSummary(currencyCodeValue = currencyCode.value)
}
