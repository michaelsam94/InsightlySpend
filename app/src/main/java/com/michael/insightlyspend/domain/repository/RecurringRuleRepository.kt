package com.michael.insightlyspend.domain.repository

import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow

interface RecurringRuleRepository {
    fun observeRecurringRules(): Flow<List<RecurringRule>>

    suspend fun upsert(
        id: Long?,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        note: String?,
        paymentMethod: PaymentMethod,
        isIncome: Boolean,
        debtDirection: DebtDirection,
        dayOfMonth: Int,
    ): Long

    suspend fun delete(id: Long)
}
