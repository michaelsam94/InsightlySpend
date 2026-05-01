package com.michael.insightlyspend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Legacy fallback / canonical sort key; prefer [nameEn] / [nameAr] when present. */
    val name: String,
    val nameEn: String? = null,
    val nameAr: String? = null,
    val iconResource: String,
    val budgetLimit: Double?,
    val rolloverUnusedToNextMonth: Boolean = false,
)
