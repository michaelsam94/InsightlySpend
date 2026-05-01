package com.michael.insightlyspend.core

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatMoney(amount: Double, currencyCode: String, locale: Locale): String {
    val currency = try {
        Currency.getInstance(currencyCode)
    } catch (_: Throwable) {
        Currency.getInstance(locale)
    }
    val nf = NumberFormat.getCurrencyInstance(locale)
    nf.currency = currency
    return nf.format(amount)
}
