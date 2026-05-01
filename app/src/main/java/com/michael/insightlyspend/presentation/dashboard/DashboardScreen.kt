package com.michael.insightlyspend.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.domain.model.Transaction
import com.michael.insightlyspend.presentation.components.SevenDayLineChart
import com.michael.insightlyspend.presentation.components.QuickAddTransactionSheet
import com.michael.insightlyspend.presentation.root.MainThemeViewModel
import com.michael.insightlyspend.presentation.util.currentAppLocale
import com.michael.insightlyspend.presentation.util.iconForCategory
import com.michael.insightlyspend.presentation.util.transactionCategoryDisplayName
import java.util.Locale

@Composable
fun DashboardScreen(
    dashboardVm: DashboardViewModel = hiltViewModel(),
    themeVm: MainThemeViewModel = hiltViewModel(),
) {
    val state by dashboardVm.state.collectAsState()
    val currency by themeVm.currencyCode.collectAsState()
    val locale = currentAppLocale()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_quick_add),
                )
            }
        },
    ) { padding ->
        when (val s = state) {
            DashboardUiState.Loading -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Error -> {
                Text(s.message, modifier = Modifier.padding(padding))
            }

            is DashboardUiState.Ready -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        SummaryCard(s.summary, currency, locale)
                    }
                    item {
                        Text(
                            stringResource(R.string.daily_spending_chart),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        SevenDayLineChart(s.summary.dailySpendLast7Days)
                    }
                    item {
                        Text(
                            stringResource(R.string.recent_transactions),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    items(s.summary.recentTransactions) { tx ->
                        TransactionRow(tx, currency, locale)
                    }
                }
            }
        }
    }

    if (showAdd) {
        QuickAddTransactionSheet(
            onDismiss = { showAdd = false },
            onSaved = { showAdd = false },
        )
    }
}

@Composable
private fun SummaryCard(
    summary: com.michael.insightlyspend.domain.model.DashboardSummary,
    currency: String,
    locale: Locale,
) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.total_balance), style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatMoney(summary.totalBalance, currency, locale),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.income_spend_label), style = MaterialTheme.typography.labelMedium)
                    Text(
                        stringResource(
                            R.string.income_spend_pair,
                            formatMoney(summary.monthlyIncome, currency, locale),
                            formatMoney(summary.monthlySpend, currency, locale),
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            summary.monthlyBudget?.let { budget ->
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.budget_progress), style = MaterialTheme.typography.labelMedium)
                val progress = summary.budgetProgress ?: 0f
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(
                        R.string.spent_of_budget,
                        formatMoney(summary.monthlySpend, currency, locale),
                        formatMoney(budget, currency, locale),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, currency: String, locale: Locale) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(iconForCategory(tx.categoryIconKey), contentDescription = null)
                Column {
                    Text(transactionCategoryDisplayName(tx), style = MaterialTheme.typography.titleSmall)
                    tx.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
            Text(
                (if (tx.isIncome) "+" else "-") + formatMoney(tx.amount, currency, locale),
                style = MaterialTheme.typography.titleSmall,
                color = if (tx.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}
