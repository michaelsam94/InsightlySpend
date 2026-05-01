package com.michael.insightlyspend.domain.repository

import com.michael.insightlyspend.domain.model.DashboardSummary
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.LedgerFilters
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun addTransaction(
        amount: Double,
        timestamp: Long,
        categoryId: Long,
        note: String?,
        imagePath: String?,
        isRecurring: Boolean,
        accountId: Long,
        paymentMethod: PaymentMethod,
        isIncome: Boolean,
        debtDirection: DebtDirection,
    ): Long

    suspend fun deleteTransaction(id: Long)

    suspend fun duplicateTransaction(id: Long)

    fun observeLedger(filters: LedgerFilters): Flow<List<Transaction>>

    suspend fun getDashboardSummary(): DashboardSummary

    /** Recomputes whenever transactions, accounts, or categories change (Room-backed). */
    fun observeDashboardSummary(): Flow<DashboardSummary>

    /** Rows that include receipt images for the vault grid */
    fun observeReceiptRows(): Flow<List<Transaction>>

    suspend fun monthlyExpenseTotal(epochMillis: Long): Double
}
