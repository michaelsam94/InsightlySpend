package com.michael.insightlyspend.domain.usecase

import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
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
    ): Long = transactionRepository.addTransaction(
        amount = amount,
        timestamp = timestamp,
        categoryId = categoryId,
        note = note,
        imagePath = imagePath,
        isRecurring = isRecurring,
        accountId = accountId,
        paymentMethod = paymentMethod,
        isIncome = isIncome,
        debtDirection = debtDirection,
    )
}
