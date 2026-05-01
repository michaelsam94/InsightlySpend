package com.michael.insightlyspend.domain.model

data class BudgetCategoryStatus(
    val categoryId: Long,
    val categoryName: String,
    val categoryNameEn: String?,
    val categoryNameAr: String?,
    /** Stable key from [com.michael.insightlyspend.domain.model.Category.iconResource] for UI localization. */
    val categoryIconKey: String,
    val budgetLimit: Double,
    val spentThisMonth: Double,
    val remaining: Double,
    val usagePercent: Float,
    val thresholdCrossed: Boolean,
)
