package com.michael.insightlyspend.domain.repository

import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategory(categoryId: Long): Category?

    /**
     * Creates a category. Provide [nameEn] and/or [nameAr]; at least one must be non-blank.
     * [monthlyBudgetLimit] optional — categories without a limit do not appear on the Budget tab until set.
     */
    suspend fun createCategory(
        nameEn: String?,
        nameAr: String?,
        monthlyBudgetLimit: Double?,
        iconResource: String = "label",
    ): Long

    suspend fun updateCategory(categoryId: Long, nameEn: String?, nameAr: String?)

    /**
     * Deletes the category after moving all transactions to another category and removing
     * recurring rules that reference it. Returns false if this would remove the last category.
     */
    suspend fun deleteCategory(categoryId: Long): Boolean

    suspend fun setBudget(categoryId: Long, monthlyLimit: Double?, rollover: Boolean)

    suspend fun totalMonthlyBudgetCap(): Double?

    suspend fun budgetStatusesForMonth(): List<BudgetCategoryStatus>
}
