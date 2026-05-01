package com.michael.insightlyspend.domain.model

data class LedgerFilters(
    val query: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val categoryId: Long? = null,
    val paymentMethod: PaymentMethod? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val debtDirection: DebtDirection? = null,
)
