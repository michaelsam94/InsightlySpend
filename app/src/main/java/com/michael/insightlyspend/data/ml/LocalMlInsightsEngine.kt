package com.michael.insightlyspend.data.ml

import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import com.michael.insightlyspend.domain.model.CategoryAnomaly
import com.michael.insightlyspend.domain.model.CategoryShare
import com.michael.insightlyspend.domain.model.ForecastMethod
import com.michael.insightlyspend.domain.model.FullMlInsightReport
import com.michael.insightlyspend.domain.model.MonthComparison
import com.michael.insightlyspend.domain.model.MonthlySpendForecast
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

@Singleton
class LocalMlInsightsEngine @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val tfliteForecastAdapter: TfliteForecastAdapter,
) {

    suspend fun buildReport(nowEpochMillis: Long): FullMlInsightReport {
        val monthStart = Time.startOfMonthMillis(nowEpochMillis)
        val nextMonthStart = Time.startOfNextMonthMillis(nowEpochMillis)

        val monthSpend = transactionDao.sumExpenseBetween(monthStart, nextMonthStart)
        val lastMonthSpend = transactionDao.sumExpenseBetween(
            Time.previousMonthRange(nowEpochMillis).first,
            monthStart,
        )

        val rows = transactionDao.sumExpenseByCategoryBetween(monthStart, nextMonthStart)
        val categories = categoryDao.getAll().associateBy { it.id }
        val safeTotal = rows.sumOf { it.total }.takeIf { it > 0 } ?: 1.0
        val shares = rows.map { row ->
            val name = categories[row.categoryId]?.name ?: "Category ${row.categoryId}"
            CategoryShare(
                categoryId = row.categoryId,
                categoryName = name,
                amount = row.total,
                percentOfTotal = ((row.total / safeTotal) * 100).toFloat(),
            )
        }.sortedByDescending { it.amount }

        val forecast = forecastMonthEnd(nowEpochMillis, monthSpend)

        val anomalies = detectCategoryAnomalies(monthStart, nextMonthStart)

        val delta = when {
            lastMonthSpend == 0.0 -> null
            else -> ((monthSpend - lastMonthSpend) / lastMonthSpend * 100).toFloat()
        }

        return FullMlInsightReport(
            forecast = forecast,
            categoryShares = shares,
            monthComparison = MonthComparison(
                thisMonthSpend = monthSpend,
                lastMonthSpend = lastMonthSpend,
                deltaPercent = delta,
            ),
            anomalies = anomalies,
        )
    }

    private suspend fun forecastMonthEnd(
        nowEpochMillis: Long,
        spentSoFar: Double,
    ): MonthlySpendForecast {
        val monthStart = Time.startOfMonthMillis(nowEpochMillis)
        val daysInMonth = Time.daysInMonth(nowEpochMillis)
        val dayIndex = Time.dayOfMonth(nowEpochMillis).coerceAtLeast(1)

        val dailyBurns = DoubleArray(dayIndex)
        val cal = Calendar.getInstance()
        for (d in 1..dayIndex) {
            cal.timeInMillis = monthStart
            cal.add(Calendar.DAY_OF_MONTH, d - 1)
            val dayStart = Time.startOfDayMillis(cal.timeInMillis)
            dailyBurns[d - 1] = transactionDao.sumExpenseForDay(dayStart, dayStart + DAY_MS)
        }

        val avgDaily = if (dayIndex > 0) spentSoFar / dayIndex else 0.0
        val naivePrediction = avgDaily * daysInMonth

        val cumulative = DoubleArray(dayIndex)
        var running = 0.0
        for (i in dailyBurns.indices) {
            running += dailyBurns[i]
            cumulative[i] = running
        }

        val xs = (1..dayIndex).map { it.toDouble() }.toDoubleArray()
        val regressionPrediction = if (xs.size >= 3) {
            val (intercept, slope) = LinearRegression.fit(xs, cumulative)
            LinearRegression.predict(intercept, slope, daysInMonth.toDouble())
        } else {
            naivePrediction
        }

        val stdDaily = if (dailyBurns.size >= 2) {
            val mean = dailyBurns.average()
            val variance = dailyBurns.sumOf { (it - mean) * (it - mean) } / (dailyBurns.size - 1)
            sqrt(variance)
        } else {
            0.0
        }
        val uncertainty = stdDaily * sqrt(max(1, daysInMonth - dayIndex).toDouble())

        val tf = tfliteForecastAdapter.predictMonthEndSpend(avgDaily)

        val predicted = when {
            tf != null && regressionPrediction > 0 -> regressionPrediction * 0.65 + tf * 0.35
            tf != null -> tf.toDouble()
            regressionPrediction > 0 -> regressionPrediction
            else -> naivePrediction
        }

        val method = when {
            tf != null && regressionPrediction > 0 -> ForecastMethod.HYBRID
            tf != null -> ForecastMethod.TFLITE_MODEL
            else -> ForecastMethod.LINEAR_REGRESSION_ON_DEVICE
        }

        return MonthlySpendForecast(
            predictedMonthEndSpend = predicted.coerceAtLeast(spentSoFar),
            confidenceLow = (predicted - 1.96 * uncertainty).coerceAtLeast(0.0),
            confidenceHigh = predicted + 1.96 * uncertainty,
            method = method,
        )
    }

    private suspend fun detectCategoryAnomalies(
        monthStart: Long,
        nextMonthStart: Long,
    ): List<CategoryAnomaly> {
        val categories = categoryDao.getAll()
        val anomalies = mutableListOf<CategoryAnomaly>()
        val cal = Calendar.getInstance()
        for (cat in categories) {
            val history = mutableListOf<Double>()
            cal.timeInMillis = monthStart
            repeat(3) {
                cal.add(Calendar.MONTH, -1)
                val start = Time.startOfMonthMillis(cal.timeInMillis)
                val end = Time.startOfNextMonthMillis(cal.timeInMillis)
                history += transactionDao.sumExpenseForCategoryBetween(cat.id, start, end)
            }
            cal.timeInMillis = monthStart

            val current = transactionDao.sumExpenseForCategoryBetween(cat.id, monthStart, nextMonthStart)
            val mean = history.average()
            val variance = if (history.size > 1) {
                history.sumOf { (it - mean) * (it - mean) } / (history.size - 1)
            } else {
                0.0
            }
            val std = sqrt(max(variance, MIN_STD))
            val z = (current - mean) / std
            if (abs(z) > 1.6 && current > mean) {
                anomalies += CategoryAnomaly(
                    categoryId = cat.id,
                    categoryName = cat.name,
                    zScore = z,
                    message = "${cat.name} is notably higher than your recent norm this month.",
                )
            }
        }
        return anomalies.sortedByDescending { abs(it.zScore) }.take(5)
    }

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
        private const val MIN_STD = 1e-3
    }
}
