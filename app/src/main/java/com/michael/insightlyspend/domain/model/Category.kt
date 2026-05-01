package com.michael.insightlyspend.domain.model

data class Category(
    val id: Long,
    val name: String,
    val nameEn: String?,
    val nameAr: String?,
    val iconResource: String,
    val budgetLimit: Double?,
    val rolloverUnusedToNextMonth: Boolean,
)
