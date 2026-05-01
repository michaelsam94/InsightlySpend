package com.michael.insightlyspend.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.michael.insightlyspend.domain.repository.AppLanguage
import com.michael.insightlyspend.domain.repository.ThemePreference
import com.michael.insightlyspend.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "insightly_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : UserPreferencesRepository {

    private val store = context.dataStore

    override val currencyCode: Flow<String> = store.data.map { prefs ->
        prefs[CURRENCY] ?: "USD"
    }

    override val themePreference: Flow<ThemePreference> = store.data.map { prefs ->
        when (prefs[THEME]) {
            "light" -> ThemePreference.LIGHT
            "dark" -> ThemePreference.DARK
            else -> ThemePreference.SYSTEM
        }
    }

    override val biometricRequired: Flow<Boolean> = store.data.map { prefs ->
        prefs[BIOMETRIC] ?: false
    }

    override val appLanguage: Flow<AppLanguage> = store.data.map { prefs ->
        when (prefs[LANGUAGE]) {
            "en" -> AppLanguage.ENGLISH
            "ar" -> AppLanguage.ARABIC
            else -> AppLanguage.SYSTEM
        }
    }

    override suspend fun setCurrencyCode(code: String) {
        store.edit { it[CURRENCY] = code }
    }

    override suspend fun setThemePreference(pref: ThemePreference) {
        store.edit { prefs ->
            prefs[THEME] = when (pref) {
                ThemePreference.SYSTEM -> "system"
                ThemePreference.LIGHT -> "light"
                ThemePreference.DARK -> "dark"
            }
        }
    }

    override suspend fun setBiometricRequired(enabled: Boolean) {
        store.edit { it[BIOMETRIC] = enabled }
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        store.edit { prefs ->
            prefs[LANGUAGE] = when (language) {
                AppLanguage.SYSTEM -> "system"
                AppLanguage.ENGLISH -> "en"
                AppLanguage.ARABIC -> "ar"
            }
        }
    }

    companion object {
        private val CURRENCY = stringPreferencesKey("currency")
        private val THEME = stringPreferencesKey("theme")
        private val BIOMETRIC = booleanPreferencesKey("biometric")
        private val LANGUAGE = stringPreferencesKey("language")
    }
}
