package com.michael.insightlyspend.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemePreference { SYSTEM, LIGHT, DARK }

interface UserPreferencesRepository {
    val currencyCode: Flow<String>
    val themePreference: Flow<ThemePreference>
    val biometricRequired: Flow<Boolean>
    val appLanguage: Flow<AppLanguage>

    suspend fun setCurrencyCode(code: String)

    suspend fun setThemePreference(pref: ThemePreference)

    suspend fun setBiometricRequired(enabled: Boolean)

    suspend fun setAppLanguage(language: AppLanguage)
}
