package com.michael.insightlyspend.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.michael.insightlyspend.R
import com.michael.insightlyspend.domain.model.Account
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.presentation.util.categoryDisplayName
import com.michael.insightlyspend.presentation.util.localizedAccountName

/**
 * Category and wallet pickers use [ExposedDropdownMenuBox] so they work inside a
 * scrolling [androidx.compose.material3.ModalBottomSheet] (plain [androidx.compose.material3.DropdownMenu] + Box
 * does not anchor correctly there).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerDropdown(
    categories: List<Category>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedId }
    val labelText = selectedCategory?.let { categoryDisplayName(it) }.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            readOnly = true,
            value = labelText,
            onValueChange = {},
            label = { Text(stringResource(R.string.category)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(categoryDisplayName(cat)) },
                    onClick = {
                        onSelected(cat.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletPickerDropdown(
    accounts: List<Account>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedId }
    val labelText = selectedAccount?.let { localizedAccountName(it) }.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            readOnly = true,
            value = labelText,
            onValueChange = {},
            label = { Text(stringResource(R.string.wallet)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            accounts.forEach { acc ->
                DropdownMenuItem(
                    text = { Text(localizedAccountName(acc)) },
                    onClick = {
                        onSelected(acc.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
