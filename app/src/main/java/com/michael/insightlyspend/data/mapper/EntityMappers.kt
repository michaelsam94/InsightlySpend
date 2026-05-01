package com.michael.insightlyspend.data.mapper

import com.michael.insightlyspend.data.local.entity.AccountEntity
import com.michael.insightlyspend.data.local.entity.CategoryEntity
import com.michael.insightlyspend.data.local.entity.TransactionEntity
import com.michael.insightlyspend.domain.model.Account
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.Transaction

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    nameEn = nameEn,
    nameAr = nameAr,
    iconResource = iconResource,
    budgetLimit = budgetLimit,
    rolloverUnusedToNextMonth = rolloverUnusedToNextMonth,
)

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    accountName = accountName,
    currentBalance = currentBalance,
)

fun TransactionEntity.toDomain(
    category: CategoryEntity,
    account: AccountEntity,
): Transaction = Transaction(
    id = id,
    amount = amount,
    timestamp = timestamp,
    categoryId = categoryId,
    categoryName = category.name,
    categoryNameEn = category.nameEn,
    categoryNameAr = category.nameAr,
    categoryIconKey = category.iconResource,
    note = note,
    imagePath = imagePath,
    isRecurring = isRecurring,
    accountId = accountId,
    accountName = account.accountName,
    paymentMethod = PaymentMethod.valueOf(paymentMethod),
    isIncome = isIncome,
    debtDirection = DebtDirection.valueOf(debtDirection),
)

fun PaymentMethod.toStorage(): String = name

fun DebtDirection.toStorage(): String = name
