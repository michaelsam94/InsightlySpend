package com.michael.insightlyspend.data.repository

import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.RecurringRuleDao
import com.michael.insightlyspend.data.local.entity.RecurringRuleEntity
import com.michael.insightlyspend.data.mapper.toStorage
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.RecurringRule
import com.michael.insightlyspend.domain.repository.RecurringRuleRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class RecurringRuleRepositoryImpl @Inject constructor(
    private val recurringRuleDao: RecurringRuleDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
) : RecurringRuleRepository {

    /**
     * Single shared combine so every UI collector sees the same Room emissions (rules, categories,
     * accounts) without duplicating cold combine pipelines per ViewModel.
     */
    private val observeRecurringRulesInternal: Flow<List<RecurringRule>> = combine(
        recurringRuleDao.observeAll(),
        categoryDao.observeCategories(),
        accountDao.observeAccounts(),
    ) { rules, categories, accounts ->
        val catMap = categories.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }
        rules.mapNotNull { r ->
            val c = catMap[r.categoryId] ?: return@mapNotNull null
            val a = accMap[r.accountId] ?: return@mapNotNull null
            RecurringRule(
                id = r.id,
                amount = r.amount,
                categoryId = r.categoryId,
                categoryName = c.name,
                accountId = r.accountId,
                accountName = a.accountName,
                note = r.note,
                paymentMethod = PaymentMethod.valueOf(r.paymentMethod),
                isIncome = r.isIncome,
                debtDirection = DebtDirection.valueOf(r.debtDirection),
                dayOfMonth = r.dayOfMonth,
                lastExecutedMonthKey = r.lastExecutedMonthKey,
            )
        }
    }

    override fun observeRecurringRules(): Flow<List<RecurringRule>> = observeRecurringRulesInternal

    override suspend fun upsert(
        id: Long?,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        note: String?,
        paymentMethod: PaymentMethod,
        isIncome: Boolean,
        debtDirection: DebtDirection,
        dayOfMonth: Int,
    ): Long {
        val clampedDay = dayOfMonth.coerceIn(1, 28)
        return if (id == null || id == 0L) {
            recurringRuleDao.insert(
                RecurringRuleEntity(
                    id = 0,
                    amount = amount,
                    categoryId = categoryId,
                    accountId = accountId,
                    note = note,
                    paymentMethod = paymentMethod.toStorage(),
                    isIncome = isIncome,
                    debtDirection = debtDirection.toStorage(),
                    dayOfMonth = clampedDay,
                    lastExecutedMonthKey = null,
                ),
            )
        } else {
            val prev = recurringRuleDao.getById(id)
            recurringRuleDao.update(
                RecurringRuleEntity(
                    id = id,
                    amount = amount,
                    categoryId = categoryId,
                    accountId = accountId,
                    note = note,
                    paymentMethod = paymentMethod.toStorage(),
                    isIncome = isIncome,
                    debtDirection = debtDirection.toStorage(),
                    dayOfMonth = clampedDay,
                    lastExecutedMonthKey = prev?.lastExecutedMonthKey,
                ),
            )
            id
        }
    }

    override suspend fun delete(id: Long) {
        recurringRuleDao.deleteById(id)
    }
}
