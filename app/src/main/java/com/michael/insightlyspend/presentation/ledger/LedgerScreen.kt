package com.michael.insightlyspend.presentation.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.Transaction
import com.michael.insightlyspend.presentation.root.MainThemeViewModel
import com.michael.insightlyspend.presentation.util.currentAppLocale
import com.michael.insightlyspend.presentation.util.iconForCategory
import com.michael.insightlyspend.presentation.util.transactionCategoryDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    vm: LedgerViewModel = hiltViewModel(),
    themeVm: MainThemeViewModel = hiltViewModel(),
) {
    val items by vm.ledger.collectAsState()
    val currency by themeVm.currencyCode.collectAsState()
    val locale = currentAppLocale()
    val labelToday = stringResource(R.string.today)
    val labelYesterday = stringResource(R.string.yesterday)
    val grouped = remember(items, locale, labelToday, labelYesterday) {
        groupByDay(items, locale, labelToday, labelYesterday)
    }
    var query by remember { mutableStateOf("") }
    var cashOnly by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.ledger_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.ledger_swipe_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                vm.updateSearch(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search_notes)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = cashOnly,
                onClick = {
                    cashOnly = !cashOnly
                    vm.setPayment(if (cashOnly) PaymentMethod.CASH else null)
                },
                label = { Text(stringResource(R.string.cash_only)) },
            )
            AssistChip(
                onClick = {
                    // Use end-of-month, not "now" at tap time — a frozen end would hide
                    // transactions (and duplicates) added later in the same month.
                    vm.setDateRange(Time.startOfMonthMillis(), Time.endOfMonthMillis())
                },
                label = { Text(stringResource(R.string.this_month)) },
            )
            AssistChip(
                onClick = {
                    vm.setDateRange(null, null)
                    vm.setPayment(null)
                },
                label = { Text(stringResource(R.string.clear_filters)) },
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            grouped.forEach { (label, rows) ->
                item(key = "h-$label") {
                    Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
                }
                items(rows, key = { it.id }) { tx ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    vm.delete(tx.id)
                                    true
                                }

                                SwipeToDismissBoxValue.StartToEnd -> {
                                    vm.duplicate(tx.id)
                                    false
                                }

                                else -> false
                            }
                        },
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = true,
                        enableDismissFromStartToEnd = true,
                        backgroundContent = {
                            val direction = dismissState.targetValue
                            val color = when (direction) {
                                SwipeToDismissBoxValue.EndToStart ->
                                    MaterialTheme.colorScheme.errorContainer

                                SwipeToDismissBoxValue.StartToEnd ->
                                    MaterialTheme.colorScheme.secondaryContainer

                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val align = when (direction) {
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                else -> Alignment.Center
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = align,
                            ) {
                                when (direction) {
                                    SwipeToDismissBoxValue.EndToStart ->
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = stringResource(R.string.cd_delete),
                                        )

                                    SwipeToDismissBoxValue.StartToEnd ->
                                        Icon(
                                            Icons.Outlined.ContentCopy,
                                            contentDescription = stringResource(R.string.cd_duplicate),
                                        )

                                    else -> Unit
                                }
                            }
                        },
                    ) {
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(iconForCategory(tx.categoryIconKey), contentDescription = null)
                                    Column {
                                        Text(transactionCategoryDisplayName(tx), fontWeight = FontWeight.SemiBold)
                                        tx.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                        Text(
                                            SimpleDateFormat("HH:mm", locale).format(Date(tx.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                                Text(
                                    (if (tx.isIncome) "+" else "-") + formatMoney(tx.amount, currency, locale),
                                    color = if (tx.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun groupByDay(
    transactions: List<Transaction>,
    locale: Locale,
    labelToday: String,
    labelYesterday: String,
): List<Pair<String, List<Transaction>>> {
    val map = linkedMapOf<String, MutableList<Transaction>>()
    val formatter = SimpleDateFormat("MMM d, yyyy", locale)
    val todayStart = Time.startOfDayMillis(Time.nowMillis())
    val yesterdayStart = todayStart - DAY_MS
    transactions.forEach { tx ->
        val dayStart = Time.startOfDayMillis(tx.timestamp)
        val label = when (dayStart) {
            todayStart -> labelToday
            yesterdayStart -> labelYesterday
            else -> formatter.format(Date(tx.timestamp))
        }
        map.getOrPut(label) { mutableListOf() }.add(tx)
    }
    return map.map { it.key to it.value }
}

private const val DAY_MS = 24L * 60L * 60L * 1000L
