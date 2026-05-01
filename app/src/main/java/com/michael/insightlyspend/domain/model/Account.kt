package com.michael.insightlyspend.domain.model

data class Account(
    val id: Long,
    val accountName: String,
    val currentBalance: Double,
)
