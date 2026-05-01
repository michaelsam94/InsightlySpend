package com.michael.insightlyspend.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.michael.insightlyspend.R
import com.michael.insightlyspend.domain.model.Account

/** Localized labels for default seeded accounts; custom accounts use the stored name. */
@Composable
fun localizedAccountName(account: Account): String {
    val resId = when (account.id) {
        1L -> R.string.account_personal_wallet
        2L -> R.string.account_business
        else -> null
    } ?: return account.accountName
    return stringResource(resId)
}
