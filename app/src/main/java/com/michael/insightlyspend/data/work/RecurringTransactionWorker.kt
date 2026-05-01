package com.michael.insightlyspend.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.local.dao.RecurringRuleDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import com.michael.insightlyspend.data.local.entity.TransactionEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recurringRuleDao: RecurringRuleDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = Time.dayOfMonth()
        val monthKey = Time.monthKey()
        val rules = recurringRuleDao.getAll()
        for (rule in rules) {
            if (rule.dayOfMonth != today) continue
            if (rule.lastExecutedMonthKey == monthKey) continue

            val entity = TransactionEntity(
                amount = rule.amount,
                timestamp = Time.nowMillis(),
                categoryId = rule.categoryId,
                note = rule.note,
                imagePath = null,
                isRecurring = true,
                accountId = rule.accountId,
                paymentMethod = rule.paymentMethod,
                isIncome = rule.isIncome,
                debtDirection = rule.debtDirection,
            )
            transactionDao.insert(entity)
            val account = accountDao.getById(rule.accountId)
            if (account != null) {
                val delta = if (rule.isIncome) rule.amount else -rule.amount
                accountDao.update(account.copy(currentBalance = account.currentBalance + delta))
            }
            recurringRuleDao.update(
                rule.copy(lastExecutedMonthKey = monthKey),
            )
        }
        return Result.success()
    }
}
