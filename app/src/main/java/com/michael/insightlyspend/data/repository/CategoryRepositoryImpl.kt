package com.michael.insightlyspend.data.repository

import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.RecurringRuleDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import com.michael.insightlyspend.data.local.entity.CategoryEntity
import com.michael.insightlyspend.data.mapper.toDomain
import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringRuleDao: RecurringRuleDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(categoryId: Long): Category? =
        categoryDao.getById(categoryId)?.toDomain()

    override suspend fun createCategory(
        nameEn: String?,
        nameAr: String?,
        monthlyBudgetLimit: Double?,
        iconResource: String,
    ): Long {
        val en = nameEn?.trim()?.takeIf { it.isNotEmpty() }
        val ar = nameAr?.trim()?.takeIf { it.isNotEmpty() }
        require(en != null || ar != null) { "At least one language name is required" }
        val canonical = en ?: ar!!
        val entity = CategoryEntity(
            name = canonical,
            nameEn = en,
            nameAr = ar,
            iconResource = iconResource.ifBlank { "label" },
            budgetLimit = monthlyBudgetLimit,
            rolloverUnusedToNextMonth = false,
        )
        return categoryDao.insert(entity)
    }

    override suspend fun updateCategory(categoryId: Long, nameEn: String?, nameAr: String?) {
        val existing = categoryDao.getById(categoryId) ?: return
        val en = nameEn?.trim()?.takeIf { it.isNotEmpty() }
        val ar = nameAr?.trim()?.takeIf { it.isNotEmpty() }
        require(en != null || ar != null) { "At least one language name is required" }
        val canonical = en ?: ar!!
        categoryDao.update(
            existing.copy(
                name = canonical,
                nameEn = en,
                nameAr = ar,
            ),
        )
    }

    override suspend fun deleteCategory(categoryId: Long): Boolean {
        val all = categoryDao.getAll()
        if (all.size <= 1) return false
        val replacement = all.firstOrNull { it.id != categoryId } ?: return false
        transactionDao.reassignCategory(categoryId, replacement.id)
        recurringRuleDao.deleteByCategoryId(categoryId)
        categoryDao.deleteById(categoryId)
        return true
    }

    override suspend fun setBudget(categoryId: Long, monthlyLimit: Double?, rollover: Boolean) {
        val existing = categoryDao.getById(categoryId) ?: return
        categoryDao.update(
            existing.copy(
                budgetLimit = monthlyLimit,
                rolloverUnusedToNextMonth = rollover,
            ),
        )
    }

    override suspend fun totalMonthlyBudgetCap(): Double? {
        val limits = categoryDao.getAll().mapNotNull { it.budgetLimit }
        return limits.takeIf { it.isNotEmpty() }?.sum()
    }

    override suspend fun budgetStatusesForMonth(): List<BudgetCategoryStatus> {
        val monthStart = Time.startOfMonthMillis()
        val nextMonthStart = Time.startOfNextMonthMillis()
        return categoryDao.getAll().mapNotNull { cat ->
            val limit = cat.budgetLimit ?: return@mapNotNull null
            val spent = transactionDao.sumExpenseForCategoryBetween(cat.id, monthStart, nextMonthStart)
            val remaining = limit - spent
            val usage = if (limit > 0) (spent / limit).toFloat() else 0f
            BudgetCategoryStatus(
                categoryId = cat.id,
                categoryName = cat.name,
                categoryNameEn = cat.nameEn,
                categoryNameAr = cat.nameAr,
                categoryIconKey = cat.iconResource,
                budgetLimit = limit,
                spentThisMonth = spent,
                remaining = remaining,
                usagePercent = usage,
                thresholdCrossed = usage >= 0.8f,
            )
        }
    }
}
