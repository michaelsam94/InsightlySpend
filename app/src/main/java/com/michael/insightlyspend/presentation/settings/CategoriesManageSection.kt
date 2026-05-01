package com.michael.insightlyspend.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import android.R as AndroidR
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.presentation.components.BilingualCategoryNameFields
import com.michael.insightlyspend.presentation.components.isBilingualCategoryInputValid
import com.michael.insightlyspend.presentation.util.categoryDisplayName
import com.michael.insightlyspend.presentation.util.currentAppLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesManageSection(vm: ManageCategoriesViewModel = hiltViewModel()) {
    val categories by vm.categories.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var pendingDelete by remember { mutableStateOf<Category?>(null) }
    var showDeleteLastError by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.categories_manage_title), style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showAdd = true }) {
                Text(stringResource(R.string.categories_manage_add))
            }
        }
        Text(
            stringResource(R.string.category_manage_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            categoryDisplayName(cat),
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { editing = cat }) {
                                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit))
                            }
                            if (categories.size > 1) {
                                IconButton(onClick = { pendingDelete = cat }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.cd_delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { toDelete ->
        val replacement = categories.firstOrNull { it.id != toDelete.id }
        if (replacement != null) {
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
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
                            vm.deleteCategory(toDelete.id) { ok ->
                                pendingDelete = null
                                if (!ok) showDeleteLastError = true
                            }
                        },
                    ) { Text(stringResource(R.string.category_delete_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        } else {
            LaunchedEffect(toDelete.id) { pendingDelete = null }
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

    if (showAdd) {
        CategoryEditorSheet(
            titleRes = R.string.categories_manage_add,
            initialEn = "",
            initialAr = "",
            onDismiss = { showAdd = false },
            onSave = { en, ar ->
                vm.createCategory(en, ar)
                showAdd = false
            },
        )
    }

    editing?.let { cat ->
        val seedEn = cat.nameEn?.takeIf { it.isNotBlank() }
        val seedAr = cat.nameAr?.takeIf { it.isNotBlank() }
        val initialEn = seedEn ?: if (seedAr == null) cat.name else ""
        val initialAr = seedAr ?: ""
        CategoryEditorSheet(
            titleRes = R.string.category_edit_title,
            initialEn = initialEn,
            initialAr = initialAr,
            onDismiss = { editing = null },
            onSave = { en, ar ->
                vm.updateCategory(cat.id, en, ar)
                editing = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorSheet(
    titleRes: Int,
    initialEn: String,
    initialAr: String,
    onDismiss: () -> Unit,
    onSave: (String?, String?) -> Unit,
) {
    var nameEn by remember(titleRes, initialEn, initialAr) { mutableStateOf(initialEn) }
    var nameAr by remember(titleRes, initialEn, initialAr) { mutableStateOf(initialAr) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isArabicUi = currentAppLocale().language == "ar"

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            BilingualCategoryNameFields(
                nameEn = nameEn,
                nameAr = nameAr,
                onEnChange = { nameEn = it },
                onArChange = { nameAr = it },
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        if (!isBilingualCategoryInputValid(nameEn, nameAr, isArabicUi)) return@Button
                        val en = nameEn.trim().takeIf { it.isNotEmpty() }
                        val ar = nameAr.trim().takeIf { it.isNotEmpty() }
                        if (en == null && ar == null) return@Button
                        onSave(en, ar)
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
