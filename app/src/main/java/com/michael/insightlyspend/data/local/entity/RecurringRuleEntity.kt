package com.michael.insightlyspend.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("accountId")],
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val accountId: Long,
    val note: String?,
    val paymentMethod: String,
    val isIncome: Boolean,
    val debtDirection: String,
    /** Day of month (1–28) when the synthetic transaction should fire */
    val dayOfMonth: Int,
    /** Format yyyy-MM — last month this rule generated a row */
    val lastExecutedMonthKey: String?,
)
