package com.michael.insightlyspend.domain.model

data class DashboardSummary(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlySpend: Double,
    val monthlyBudget: Double?,
    val budgetProgress: Float?,
    val recentTransactions: List<Transaction>,
    val dailySpendLast7Days: List<DailySpendPoint>,
)

data class DailySpendPoint(
    val dayEpochMillis: Long,
    val totalExpense: Double,
)
