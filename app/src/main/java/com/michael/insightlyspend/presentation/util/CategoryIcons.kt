package com.michael.insightlyspend.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

fun iconForCategory(key: String): ImageVector =
    when (key.lowercase()) {
        "home" -> Icons.Outlined.Home
        "restaurant", "food", "dining" -> Icons.Outlined.Restaurant
        "directions_car", "transport", "car" -> Icons.Outlined.DirectionsCar
        "movie", "entertainment" -> Icons.Outlined.LocalMovies
        "bolt", "utilities" -> Icons.Outlined.Bolt
        "payments", "income" -> Icons.Outlined.Payments
        "business" -> Icons.Outlined.AccountBalance
        else -> Icons.Outlined.Label
    }

@Composable
fun rememberCategoryIcon(key: String): ImageVector = iconForCategory(key)
