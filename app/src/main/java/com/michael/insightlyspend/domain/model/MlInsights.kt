package com.michael.insightlyspend.domain.model

/**
 * On-device insights: statistical forecast plus optional TFLite overlay when a model is bundled.
 */
data class MonthlySpendForecast(
    val predictedMonthEndSpend: Double,
    val confidenceLow: Double,
    val confidenceHigh: Double,
    val method: ForecastMethod,
)

enum class ForecastMethod {
    LINEAR_REGRESSION_ON_DEVICE,
    TFLITE_MODEL,
    HYBRID,
}

data class CategoryShare(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val percentOfTotal: Float,
)

data class MonthComparison(
    val thisMonthSpend: Double,
    val lastMonthSpend: Double,
    val deltaPercent: Float?,
)

data class CategoryAnomaly(
    val categoryId: Long,
    val categoryName: String,
    val zScore: Double,
    val message: String,
)

data class FullMlInsightReport(
    val forecast: MonthlySpendForecast,
    val categoryShares: List<CategoryShare>,
    val monthComparison: MonthComparison,
    val anomalies: List<CategoryAnomaly>,
)
