package com.michael.insightlyspend.data.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.budgetAlertStore: DataStore<Preferences> by preferencesDataStore(name = "budget_alerts")

@Singleton
class BudgetAlertPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val store = context.budgetAlertStore

    private val keysPref = stringSetPreferencesKey("sent_keys")

    suspend fun hasAcknowledged(key: String): Boolean =
        store.data.map { prefs ->
            prefs[keysPref]?.contains(key) == true
        }.first()

    suspend fun acknowledge(key: String) {
        store.edit { prefs ->
            val cur = prefs[keysPref].orEmpty().toMutableSet()
            cur.add(key)
            prefs[keysPref] = cur
        }
    }

    suspend fun pruneExceptMonthPrefix(monthPrefix: String) {
        store.edit { prefs ->
            val cur = prefs[keysPref].orEmpty().filter { it.startsWith(monthPrefix) }.toSet()
            prefs[keysPref] = cur
        }
    }
}
