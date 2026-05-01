package com.michael.insightlyspend.domain.usecase

import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.domain.repository.TransactionRepository
import javax.inject.Inject

class GetMonthlyExpensesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(epochMillis: Long = Time.nowMillis()): Double =
        transactionRepository.monthlyExpenseTotal(epochMillis)
}
