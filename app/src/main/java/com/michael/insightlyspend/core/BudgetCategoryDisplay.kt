package com.michael.insightlyspend.core

import android.content.Context
import com.michael.insightlyspend.R
import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import java.util.Locale

fun Context.resolveBudgetCategoryDisplayName(row: BudgetCategoryStatus): String {
    val locList = resources.configuration.locales
    val locale = if (locList.size() > 0) locList[0] else Locale.getDefault()
    val ar = row.categoryNameAr?.trim()?.takeIf { it.isNotEmpty() }
    val en = row.categoryNameEn?.trim()?.takeIf { it.isNotEmpty() }
    val picked = when (locale.language) {
        "ar" -> ar ?: en
        else -> en ?: ar
    }
    if (!picked.isNullOrBlank()) return picked
    val preset = categoryLabelResId(row.categoryIconKey)
    return if (preset != null) getString(preset) else row.categoryName
}
