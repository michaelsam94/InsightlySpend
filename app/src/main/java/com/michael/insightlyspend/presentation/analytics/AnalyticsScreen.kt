package com.michael.insightlyspend.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.presentation.components.CategoryBarChart
import com.michael.insightlyspend.presentation.components.CategoryPieChart
import com.michael.insightlyspend.presentation.root.MainThemeViewModel
import com.michael.insightlyspend.presentation.util.currentAppLocale

@Composable
fun AnalyticsScreen(
    vm: AnalyticsViewModel = hiltViewModel(),
    themeVm: MainThemeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val currency by themeVm.currencyCode.collectAsState()
    val locale = currentAppLocale()

    when (val s = state) {
        AnalyticsUiState.Loading -> {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is AnalyticsUiState.Error -> Text(s.message, modifier = Modifier.padding(16.dp))

        is AnalyticsUiState.Ready -> {
            val report = s.report
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.analytics_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.forecast_title, report.forecast.method.name),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(
                                R.string.predicted_month_end,
                                formatMoney(report.forecast.predictedMonthEndSpend, currency, locale),
                            ),
                        )
                        Text(
                            stringResource(
                                R.string.range_confidence,
                                formatMoney(report.forecast.confidenceLow, currency, locale),
                                formatMoney(report.forecast.confidenceHigh, currency, locale),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            stringResource(R.string.forecast_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.month_comparison), fontWeight = FontWeight.Bold)
                        val cmp = report.monthComparison
                        Text(
                            stringResource(
                                R.string.analytics_this_month,
                                formatMoney(cmp.thisMonthSpend, currency, locale),
                            ),
                        )
                        Text(
                            stringResource(
                                R.string.analytics_last_month,
                                formatMoney(cmp.lastMonthSpend, currency, locale),
                            ),
                        )
                        cmp.deltaPercent?.let {
                            Text(stringResource(R.string.change_percent, it))
                        }
                    }
                }

                Text(stringResource(R.string.category_breakdown), style = MaterialTheme.typography.titleMedium)
                CategoryPieChart(report.categoryShares)
                Spacer(Modifier.height(8.dp))
                CategoryBarChart(report.categoryShares)

                if (report.anomalies.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.anomaly_hints), fontWeight = FontWeight.Bold)
                            report.anomalies.forEach {
                                Text(stringResource(R.string.anomaly_line, it.message, it.zScore))
                            }
                        }
                    }
                }
            }
        }
    }
}
