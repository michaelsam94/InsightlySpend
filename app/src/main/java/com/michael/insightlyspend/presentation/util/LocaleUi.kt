package com.michael.insightlyspend.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/** Locale after [androidx.appcompat.app.AppCompatDelegate] per-app language is applied. */
@Composable
fun currentAppLocale(): Locale {
    val locales = LocalConfiguration.current.locales
    return if (locales.size() > 0) locales[0] else Locale.getDefault()
}
