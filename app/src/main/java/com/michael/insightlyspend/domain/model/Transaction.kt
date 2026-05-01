package com.michael.insightlyspend.domain.model

data class Transaction(
    val id: Long,
    val amount: Double,
    val timestamp: Long,
    val categoryId: Long,
    val categoryName: String,
    val categoryNameEn: String?,
    val categoryNameAr: String?,
    val categoryIconKey: String,
    val note: String?,
    val imagePath: String?,
    val isRecurring: Boolean,
    val accountId: Long,
    val accountName: String,
    val paymentMethod: PaymentMethod,
    val isIncome: Boolean,
    val debtDirection: DebtDirection,
)
