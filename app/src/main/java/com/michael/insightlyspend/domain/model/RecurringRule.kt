package com.michael.insightlyspend.domain.model

data class RecurringRule(
    val id: Long,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String,
    val accountId: Long,
    val accountName: String,
    val note: String?,
    val paymentMethod: PaymentMethod,
    val isIncome: Boolean,
    val debtDirection: DebtDirection,
    val dayOfMonth: Int,
    val lastExecutedMonthKey: String?,
)
