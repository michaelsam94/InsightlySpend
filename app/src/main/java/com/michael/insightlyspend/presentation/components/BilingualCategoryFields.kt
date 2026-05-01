package com.michael.insightlyspend.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.michael.insightlyspend.R
import com.michael.insightlyspend.presentation.util.currentAppLocale

@Composable
fun BilingualCategoryNameFields(
    nameEn: String,
    nameAr: String,
    onEnChange: (String) -> Unit,
    onArChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isArabicUi = currentAppLocale().language == "ar"
    val arLabel = stringResource(
        if (isArabicUi) R.string.category_ar_required else R.string.category_ar_optional,
    )
    val enLabel = stringResource(
        if (isArabicUi) R.string.category_en_optional else R.string.category_en_required,
    )

    // Arabic field first in Arabic UI, else English first
    if (isArabicUi) {
        OutlinedTextField(
            value = nameAr,
            onValueChange = onArChange,
            label = { Text(arLabel) },
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = nameEn,
            onValueChange = onEnChange,
            label = { Text(enLabel) },
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )
    } else {
        OutlinedTextField(
            value = nameEn,
            onValueChange = onEnChange,
            label = { Text(enLabel) },
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = nameAr,
            onValueChange = onArChange,
            label = { Text(arLabel) },
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )
    }
}

/** Returns true if primary name for current UI language is non-blank. */
fun isBilingualCategoryInputValid(nameEn: String, nameAr: String, isArabicUi: Boolean): Boolean {
    val en = nameEn.trim().isNotEmpty()
    val ar = nameAr.trim().isNotEmpty()
    return if (isArabicUi) ar else en
}
