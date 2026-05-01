package com.michael.insightlyspend.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.repository.AppLanguage
import com.michael.insightlyspend.domain.repository.ThemePreference
import com.michael.insightlyspend.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainThemeViewModel @Inject constructor(
    prefs: UserPreferencesRepository,
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> =
        prefs.themePreference.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemePreference.SYSTEM,
        )

    val biometricRequired: StateFlow<Boolean> =
        prefs.biometricRequired.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    val currencyCode: StateFlow<String> =
        prefs.currencyCode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "USD",
        )

    val appLanguage: StateFlow<AppLanguage> =
        prefs.appLanguage.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppLanguage.SYSTEM,
        )
}
