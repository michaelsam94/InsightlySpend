package com.michael.insightlyspend.domain.model

enum class PaymentMethod { CASH, CARD }

enum class DebtDirection {
    NONE,
    I_OWE,
    OWED_TO_ME,
}
