package com.michael.insightlyspend.presentation.budget

import android.R as AndroidR
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.core.formatMoney
import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.presentation.root.MainThemeViewModel
import com.michael.insightlyspend.presentation.settings.ManageCategoriesViewModel
import com.michael.insightlyspend.presentation.util.categoryDisplayName
import com.michael.insightlyspend.presentation.util.currentAppLocale
import com.michael.insightlyspend.presentation.components.BilingualCategoryNameFields
import com.michael.insightlyspend.presentation.components.isBilingualCategoryInputValid
import com.michael.insightlyspend.presentation.util.budgetCategoryDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    vm: BudgetViewModel = hiltViewModel(),
    themeVm: MainThemeViewModel = hiltViewModel(),
    manageCategoriesVm: ManageCategoriesViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val currency by themeVm.currencyCode.collectAsState()
    val locale = currentAppLocale()
    val allCategories by manageCategoriesVm.categories.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<BudgetCategoryStatus?>(null) }
    var pendingDeleteRow by remember { mutableStateOf<BudgetCategoryStatus?>(null) }
    var showDeleteLastError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.budget_add_category_cd),
                )
            }
        },
    ) { innerPadding ->
        when (val s = state) {
            BudgetUiState.Loading -> Text(
                stringResource(R.string.loading_budgets),
                modifier = Modifier.padding(innerPadding).padding(16.dp),
            )

            is BudgetUiState.Error -> Text(
                s.message,
                modifier = Modifier.padding(innerPadding).padding(16.dp),
            )

            is BudgetUiState.Ready -> {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.budget_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.budget_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(s.rows, key = { it.categoryId }) { row ->
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = if (row.thresholdCrossed) {
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                )
                            } else {
                                CardDefaults.cardColors()
                            },
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        budgetCategoryDisplayName(row),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    IconButton(onClick = { editingRow = row }) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = stringResource(R.string.edit),
                                        )
                                    }
                                    if (allCategories.size > 1) {
                                        IconButton(onClick = { pendingDeleteRow = row }) {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = stringResource(R.string.cd_delete),
                                            )
                                        }
                                    }
                                }
                                Text(
                                    stringResource(
                                        R.string.budget_remaining,
                                        formatMoney(row.remaining, currency, locale),
                                    ),
                                )
                                Text(
                                    stringResource(
                                        R.string.budget_spent_line,
                                        formatMoney(row.spentThisMonth, currency, locale),
                                        formatMoney(row.budgetLimit, currency, locale),
                                    ),
                                )
                                LinearProgressIndicator(
                                    progress = {
                                        row.usagePercent.coerceIn(0f, 1.5f).coerceAtMost(1f)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                if (row.thresholdCrossed) {
                                    Text(
                                        stringResource(R.string.threshold_alert),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddBudgetCategorySheet(
            currencyCode = currency,
            onDismiss = { showAddSheet = false },
            onSubmit = { nameEn, nameAr, limit ->
                vm.createCustomBudgetCategory(nameEn, nameAr, limit)
                showAddSheet = false
            },
        )
    }

    editingRow?.let { row ->
        EditBudgetCategorySheet(
            row = row,
            currencyCode = currency,
            onDismiss = { editingRow = null },
            onSubmit = { nameEn, nameAr, limit ->
                vm.updateBudgetCategory(row.categoryId, nameEn, nameAr, limit)
                editingRow = null
            },
        )
    }

    pendingDeleteRow?.let { toDelete ->
        val replacement = allCategories.firstOrNull { it.id != toDelete.categoryId }
        if (replacement != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteRow = null },
                title = { Text(stringResource(R.string.category_delete_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.category_delete_message,
                            categoryDisplayName(replacement),
                        ),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            manageCategoriesVm.deleteCategory(toDelete.categoryId) { ok ->
                                pendingDeleteRow = null
                                if (ok) vm.refresh()
                                if (!ok) showDeleteLastError = true
                            }
                        },
                    ) { Text(stringResource(R.string.category_delete_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteRow = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        } else {
            LaunchedEffect(toDelete.categoryId) { pendingDeleteRow = null }
        }
    }

    if (showDeleteLastError) {
        AlertDialog(
            onDismissRequest = { showDeleteLastError = false },
            text = { Text(stringResource(R.string.category_delete_last)) },
            confirmButton = {
                TextButton(onClick = { showDeleteLastError = false }) {
                    Text(stringResource(AndroidR.string.ok))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBudgetCategorySheet(
    row: BudgetCategoryStatus,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSubmit: (String?, String?, Double) -> Unit,
) {
    val seedEn = row.categoryNameEn?.takeIf { it.isNotBlank() }
    val seedAr = row.categoryNameAr?.takeIf { it.isNotBlank() }
    var nameEn by remember(row.categoryId) {
        mutableStateOf(seedEn ?: if (seedAr == null) row.categoryName else "")
    }
    var nameAr by remember(row.categoryId) { mutableStateOf(seedAr ?: "") }
    var limitText by remember(row.categoryId) { mutableStateOf(row.budgetLimit.toString()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isArabicUi = currentAppLocale().language == "ar"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(R.string.category_edit_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(12.dp))
            BilingualCategoryNameFields(
                nameEn = nameEn,
                nameAr = nameAr,
                onEnChange = { nameEn = it },
                onArChange = { nameAr = it },
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it },
                label = {
                    Text(
                        stringResource(R.string.budget_custom_limit_hint) + " ($currencyCode)",
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        if (!isBilingualCategoryInputValid(nameEn, nameAr, isArabicUi)) return@Button
                        val lim = limitText.toDoubleOrNull() ?: return@Button
                        if (lim <= 0) return@Button
                        val en = nameEn.trim().takeIf { it.isNotEmpty() }
                        val ar = nameAr.trim().takeIf { it.isNotEmpty() }
                        if (en == null && ar == null) return@Button
                        onSubmit(en, ar, lim)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.save))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetCategorySheet(
    currencyCode: String,
    onDismiss: () -> Unit,
    onSubmit: (String?, String?, Double) -> Unit,
) {
    var nameEn by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isArabicUi = currentAppLocale().language == "ar"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(R.string.budget_new_category_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(12.dp))
            BilingualCategoryNameFields(
                nameEn = nameEn,
                nameAr = nameAr,
                onEnChange = { nameEn = it },
                onArChange = { nameAr = it },
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it },
                label = {
                    Text(
                        stringResource(R.string.budget_custom_limit_hint) + " ($currencyCode)",
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        if (!isBilingualCategoryInputValid(nameEn, nameAr, isArabicUi)) return@Button
                        val lim = limitText.toDoubleOrNull() ?: return@Button
                        if (lim <= 0) return@Button
                        val en = nameEn.trim().takeIf { it.isNotEmpty() }
                        val ar = nameAr.trim().takeIf { it.isNotEmpty() }
                        if (en == null && ar == null) return@Button
                        onSubmit(en, ar, lim)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.budget_add_category_submit))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
