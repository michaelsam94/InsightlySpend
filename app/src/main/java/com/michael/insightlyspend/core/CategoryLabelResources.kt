package com.michael.insightlyspend.core

import com.michael.insightlyspend.R

/** String resource for seeded categories (keys match DB `iconResource`). */
fun categoryLabelResId(iconKey: String): Int? =
    when (iconKey.lowercase()) {
        "home" -> R.string.cat_housing
        "restaurant" -> R.string.cat_food_dining
        "directions_car" -> R.string.cat_transport
        "movie" -> R.string.cat_entertainment
        "bolt" -> R.string.cat_utilities
        "payments" -> R.string.cat_income
        else -> null
    }
