package com.michael.insightlyspend.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.RecurringRule
import com.michael.insightlyspend.presentation.components.CategoryPickerDropdown
import com.michael.insightlyspend.presentation.components.WalletPickerDropdown
import com.michael.insightlyspend.presentation.root.MainThemeViewModel
import com.michael.insightlyspend.presentation.util.currentAppLocale
import com.michael.insightlyspend.presentation.util.categoryDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringRulesSection(
    vm: RecurringRulesViewModel = hiltViewModel(),
    themeVm: MainThemeViewModel = hiltViewModel(),
    categoriesVm: QuickCategoriesAccountsViewModel = hiltViewModel(),
) {
    val rules by vm.rules.collectAsState()
    val categories by categoriesVm.categories.collectAsState()
    val currency by themeVm.currencyCode.collectAsState()
    val locale = currentAppLocale()
    var editorTarget by remember { mutableStateOf<RecurringRule?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.recurring_title), style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showCreate = true }) { Text(stringResource(R.string.add_rule)) }
        }
        Text(
            stringResource(R.string.recurring_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (rules.isEmpty()) {
            Text(stringResource(R.string.no_recurring_rules), style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rules.forEach { rule ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    categories.find { it.id == rule.categoryId }
                                        ?.let { categoryDisplayName(it) }
                                        ?: rule.categoryName,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    stringResource(
                                        R.string.recurring_rule_line,
                                        formatMoney(rule.amount, currency, locale),
                                        rule.dayOfMonth,
                                        rule.accountName,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                rule.note?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                            }
                            IconButton(onClick = { editorTarget = rule }) {
                                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit))
                            }
                            IconButton(onClick = { vm.delete(rule.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.cd_delete))
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorTarget != null || showCreate) {
        RecurringRuleEditorSheet(
            initial = editorTarget,
            currencyCode = currency,
            onDismiss = {
                editorTarget = null
                showCreate = false
            },
            onSave = { payload ->
                vm.upsert(
                    id = payload.id,
                    amount = payload.amount,
                    categoryId = payload.categoryId,
                    accountId = payload.accountId,
                    note = payload.note,
                    paymentMethod = payload.paymentMethod,
                    isIncome = payload.isIncome,
                    debtDirection = payload.debtDirection,
                    dayOfMonth = payload.dayOfMonth,
                )
                editorTarget = null
                showCreate = false
            },
        )
    }
}

private data class EditorPayload(
    val id: Long?,
    val amount: Double,
    val categoryId: Long,
    val accountId: Long,
    val note: String?,
    val paymentMethod: PaymentMethod,
    val isIncome: Boolean,
    val debtDirection: DebtDirection,
    val dayOfMonth: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringRuleEditorSheet(
    initial: RecurringRule?,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (EditorPayload) -> Unit,
    categoriesVm: QuickCategoriesAccountsViewModel = hiltViewModel(),
) {
    val categories by categoriesVm.categories.collectAsState()
    val accounts by categoriesVm.accounts.collectAsState()

    var amountText by remember(initial?.id) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var note by remember(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var categoryId by remember(initial?.id) {
        mutableStateOf(initial?.categoryId ?: categories.firstOrNull()?.id)
    }
    var accountId by remember(initial?.id) {
        mutableStateOf(initial?.accountId ?: accounts.firstOrNull()?.id)
    }
    var income by remember(initial?.id) { mutableStateOf(initial?.isIncome ?: false) }
    var cash by remember(initial?.id) {
        mutableStateOf(initial?.paymentMethod != PaymentMethod.CARD)
    }
    var debt by remember(initial?.id) { mutableStateOf(initial?.debtDirection ?: DebtDirection.NONE) }
    var dayText by remember(initial?.id) {
        mutableStateOf((initial?.dayOfMonth ?: 1).toString())
    }
    LaunchedEffect(categories, accounts) {
        if (categoryId == null) categoryId = categories.firstOrNull()?.id
        if (accountId == null) accountId = accounts.firstOrNull()?.id
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(
                    if (initial == null) R.string.new_recurring_rule else R.string.edit_recurring_rule,
                ),
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.amount_currency_label, currencyCode)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            CategoryPickerDropdown(
                categories = categories,
                selectedId = categoryId,
                onSelected = { categoryId = it },
                modifier = Modifier.fillMaxWidth(),
            )

            WalletPickerDropdown(
                accounts = accounts,
                selectedId = accountId,
                onSelected = { accountId = it },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = dayText,
                onValueChange = { dayText = it.filter { ch -> ch.isDigit() }.take(2) },
                label = { Text(stringResource(R.string.day_of_month_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.income))
                Switch(checked = income, onCheckedChange = { income = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.cash_payment))
                Switch(checked = cash, onCheckedChange = { cash = it })
            }

            Text(stringResource(R.string.debt_mode), style = MaterialTheme.typography.labelLarge)
            DebtRow(debt) { debt = it }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: return@Button
                    val cat = categoryId ?: return@Button
                    val acc = accountId ?: return@Button
                    val dom = dayText.toIntOrNull()?.coerceIn(1, 28) ?: return@Button
                    onSave(
                        EditorPayload(
                            id = initial?.id,
                            amount = amt,
                            categoryId = cat,
                            accountId = acc,
                            note = note.ifBlank { null },
                            paymentMethod = if (cash) PaymentMethod.CASH else PaymentMethod.CARD,
                            isIncome = income,
                            debtDirection = debt,
                            dayOfMonth = dom,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.save_rule)) }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DebtRow(current: DebtDirection, onSelect: (DebtDirection) -> Unit) {
    Column {
        Button(onClick = { onSelect(DebtDirection.NONE) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (current == DebtDirection.NONE) {
                    stringResource(R.string.debt_standard_sel)
                } else {
                    stringResource(R.string.debt_standard)
                },
            )
        }
        Button(onClick = { onSelect(DebtDirection.I_OWE) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (current == DebtDirection.I_OWE) {
                    stringResource(R.string.debt_i_owe_sel)
                } else {
                    stringResource(R.string.debt_i_owe_short)
                },
            )
        }
        Button(onClick = { onSelect(DebtDirection.OWED_TO_ME) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (current == DebtDirection.OWED_TO_ME) {
                    stringResource(R.string.debt_owed_sel)
                } else {
                    stringResource(R.string.debt_owed_short)
                },
            )
        }
    }
}
