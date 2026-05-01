package com.michael.insightlyspend.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.core.content.FileProvider
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.michael.insightlyspend.R
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import java.io.File

/**
 * OCR-ready: wire CameraX + ML Kit Text Recognition here to pre-fill amount / merchant note.
 *
 * Category / wallet use [ExposedDropdownMenuBox] so taps work inside a scrolling bottom sheet
 * (plain overlays + [fillMaxSize] break under unbounded height from [verticalScroll]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTransactionSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    vm: QuickAddViewModel = hiltViewModel(),
) {
    val categories by vm.categories.collectAsState()
    val accounts by vm.accounts.collectAsState()

    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var income by remember { mutableStateOf(false) }
    var cash by remember { mutableStateOf(true) }
    var debtMode by remember { mutableStateOf(DebtDirection.NONE) }
    var recurring by remember { mutableStateOf(false) }
    var receiptImageUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val pickReceiptImage = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            receiptImageUri = uri.toString()
        }
    }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takeReceiptPhoto = rememberLauncherForActivityResult(
        contract = TakePicture(),
    ) { success ->
        if (success) {
            pendingCameraUri?.let { receiptImageUri = it.toString() }
        }
        pendingCameraUri = null
    }

    LaunchedEffect(categories, accounts) {
        if (categoryId == null) categoryId = categories.firstOrNull()?.id
        if (accountId == null) accountId = accounts.firstOrNull()?.id
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(stringResource(R.string.quick_add_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            CategoryPickerDropdown(
                categories = categories,
                selectedId = categoryId,
                onSelected = { categoryId = it },
            )

            Spacer(Modifier.height(8.dp))

            WalletPickerDropdown(
                accounts = accounts,
                selectedId = accountId,
                onSelected = { accountId = it },
            )

            Spacer(Modifier.height(8.dp))
            RowSwitch(stringResource(R.string.income), income) { income = it }
            RowSwitch(stringResource(R.string.cash_payment), cash) { cash = it }
            RowSwitch(stringResource(R.string.recurring_template), recurring) { recurring = it }

            Text(stringResource(R.string.debt_lending), style = MaterialTheme.typography.labelLarge)
            DebtToggleRow(debtMode) { debtMode = it }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = {
                        pickReceiptImage.launch(
                            PickVisualMediaRequest(PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.quick_add_attach_receipt))
                }
                OutlinedButton(
                    onClick = {
                        val uri = createReceiptCameraUri(context)
                        pendingCameraUri = uri
                        takeReceiptPhoto.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.quick_add_take_photo))
                }
            }
            if (receiptImageUri != null) {
                TextButton(
                    onClick = { receiptImageUri = null },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.quick_add_remove_attachment))
                }
            }
            if (receiptImageUri != null) {
                Text(
                    stringResource(R.string.quick_add_receipt_attached),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: return@Button
                    val cat = categoryId ?: return@Button
                    val acc = accountId ?: return@Button
                    vm.saveTransaction(
                        amount = amt,
                        categoryId = cat,
                        accountId = acc,
                        note = note.ifBlank { null },
                        imagePath = receiptImageUri,
                        paymentMethod = if (cash) PaymentMethod.CASH else PaymentMethod.CARD,
                        isIncome = income,
                        debtDirection = debtMode,
                        markRecurringTemplate = recurring,
                        onDone = onSaved,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun createReceiptCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
    val file = File.createTempFile("receipt_", ".jpg", dir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

@Composable
private fun RowSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun DebtToggleRow(
    current: DebtDirection,
    onSelect: (DebtDirection) -> Unit,
) {
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
        Spacer(Modifier.height(4.dp))
        Button(onClick = { onSelect(DebtDirection.I_OWE) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (current == DebtDirection.I_OWE) {
                    stringResource(R.string.debt_i_owe_sel)
                } else {
                    stringResource(R.string.debt_i_owe_long)
                },
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = { onSelect(DebtDirection.OWED_TO_ME) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (current == DebtDirection.OWED_TO_ME) {
                    stringResource(R.string.debt_owed_sel)
                } else {
                    stringResource(R.string.debt_owed_long)
                },
            )
        }
    }
}
