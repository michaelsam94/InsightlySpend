package com.michael.insightlyspend.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.michael.insightlyspend.core.categoryLabelResId
import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.domain.model.Transaction

/**
 * Preset categories without DB translations — uses `strings.xml` via icon key.
 */
@Composable
fun localizedCategoryName(iconKey: String, storedName: String): String {
    val id = categoryLabelResId(iconKey) ?: return storedName
    return stringResource(id)
}

@Composable
fun categoryDisplayName(category: Category): String {
    val ar = category.nameAr?.trim()?.takeIf { it.isNotEmpty() }
    val en = category.nameEn?.trim()?.takeIf { it.isNotEmpty() }
    val picked = when (currentAppLocale().language) {
        "ar" -> ar ?: en
        else -> en ?: ar
    }
    if (!picked.isNullOrBlank()) return picked
    return localizedCategoryName(category.iconResource, category.name)
}

@Composable
fun budgetCategoryDisplayName(row: BudgetCategoryStatus): String {
    val ar = row.categoryNameAr?.trim()?.takeIf { it.isNotEmpty() }
    val en = row.categoryNameEn?.trim()?.takeIf { it.isNotEmpty() }
    val picked = when (currentAppLocale().language) {
        "ar" -> ar ?: en
        else -> en ?: ar
    }
    if (!picked.isNullOrBlank()) return picked
    return localizedCategoryName(row.categoryIconKey, row.categoryName)
}

@Composable
fun transactionCategoryDisplayName(tx: Transaction): String {
    val ar = tx.categoryNameAr?.trim()?.takeIf { it.isNotEmpty() }
    val en = tx.categoryNameEn?.trim()?.takeIf { it.isNotEmpty() }
    val picked = when (currentAppLocale().language) {
        "ar" -> ar ?: en
        else -> en ?: ar
    }
    if (!picked.isNullOrBlank()) return picked
    return localizedCategoryName(tx.categoryIconKey, tx.categoryName)
}
