package com.michael.insightlyspend.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.michael.insightlyspend.R

enum class AppRoutes(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Dashboard("dashboard", R.string.nav_home, Icons.Outlined.Dashboard),
    Ledger("ledger", R.string.nav_ledger, Icons.Outlined.ListAlt),
    Analytics("analytics", R.string.nav_insights, Icons.Outlined.Analytics),
    Budget("budget", R.string.nav_budget, Icons.Outlined.AccountBalanceWallet),
    Receipts("receipts", R.string.nav_vault, Icons.Outlined.GridOn),
    Settings("settings", R.string.nav_settings, Icons.Outlined.Settings),
}
