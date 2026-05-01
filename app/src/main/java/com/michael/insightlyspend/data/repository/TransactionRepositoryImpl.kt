package com.michael.insightlyspend.data.repository

import com.michael.insightlyspend.core.Time
import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import com.michael.insightlyspend.data.local.entity.TransactionEntity
import com.michael.insightlyspend.data.mapper.toDomain
import com.michael.insightlyspend.data.mapper.toStorage
import com.michael.insightlyspend.domain.model.DashboardSummary
import com.michael.insightlyspend.domain.model.DailySpendPoint
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.LedgerFilters
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.Transaction
import com.michael.insightlyspend.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
) : TransactionRepository {

    override suspend fun addTransaction(
        amount: Double,
        timestamp: Long,
        categoryId: Long,
        note: String?,
        imagePath: String?,
        isRecurring: Boolean,
        accountId: Long,
        paymentMethod: PaymentMethod,
        isIncome: Boolean,
        debtDirection: DebtDirection,
    ): Long {
        val entity = TransactionEntity(
            amount = amount,
            timestamp = timestamp,
            categoryId = categoryId,
            note = note,
            imagePath = imagePath,
            isRecurring = isRecurring,
            accountId = accountId,
            paymentMethod = paymentMethod.toStorage(),
            isIncome = isIncome,
            debtDirection = debtDirection.toStorage(),
        )
        val id = transactionDao.insert(entity)
        val account = accountDao.getById(accountId) ?: return id
        val delta = if (isIncome) amount else -amount
        accountDao.update(account.copy(currentBalance = account.currentBalance + delta))
        return id
    }

    override suspend fun deleteTransaction(id: Long) {
        val existing = transactionDao.getById(id) ?: return
        val account = accountDao.getById(existing.accountId) ?: return
        val delta = if (existing.isIncome) -existing.amount else existing.amount
        transactionDao.deleteById(id)
        accountDao.update(account.copy(currentBalance = account.currentBalance + delta))
    }

    override suspend fun duplicateTransaction(id: Long) {
        val existing = transactionDao.getById(id) ?: return
        addTransaction(
            amount = existing.amount,
            timestamp = Time.nowMillis(),
            categoryId = existing.categoryId,
            note = existing.note,
            imagePath = existing.imagePath,
            isRecurring = existing.isRecurring,
            accountId = existing.accountId,
            paymentMethod = PaymentMethod.valueOf(existing.paymentMethod),
            isIncome = existing.isIncome,
            debtDirection = DebtDirection.valueOf(existing.debtDirection),
        )
    }

    override fun observeLedger(filters: LedgerFilters): Flow<List<Transaction>> =
        transactionDao.observeFiltered(
            start = filters.startDate,
            end = filters.endDate,
            categoryId = filters.categoryId,
            paymentMethod = filters.paymentMethod?.name,
            minAmount = filters.minAmount,
            maxAmount = filters.maxAmount,
            debtDirection = filters.debtDirection?.name,
            query = filters.query.trim(),
        ).mapLatest { entities ->
            if (entities.isEmpty()) return@mapLatest emptyList()
            val cats = categoryDao.getAll().associateBy { it.id }
            val accs = accountDao.getAll().associateBy { it.id }
            entities.mapNotNull { e ->
                val c = cats[e.categoryId] ?: return@mapNotNull null
                val a = accs[e.accountId] ?: return@mapNotNull null
                e.toDomain(c, a)
            }
        }

    override suspend fun getDashboardSummary(): DashboardSummary {
        val monthStart = Time.startOfMonthMillis()
        val nextMonthStart = Time.startOfNextMonthMillis()
        val accounts = accountDao.getAll()
        val totalBalance = accounts.sumOf { it.currentBalance }
        val monthlyIncome = transactionDao.sumIncomeBetween(monthStart, nextMonthStart)
        val monthlySpend = transactionDao.sumExpenseBetween(monthStart, nextMonthStart)

        val categories = categoryDao.getAll()
        val monthlyBudget = categories.mapNotNull { it.budgetLimit }.takeIf { it.isNotEmpty() }?.sum()
        val budgetProgress = if (monthlyBudget != null && monthlyBudget > 0) {
            (monthlySpend / monthlyBudget).toFloat().coerceIn(0f, 1.5f)
        } else {
            null
        }

        val recentEntities = transactionDao.getRecent(5)
        val catMap = categories.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }
        val recent = recentEntities.mapNotNull { e ->
            val c = catMap[e.categoryId] ?: return@mapNotNull null
            val a = accMap[e.accountId] ?: return@mapNotNull null
            e.toDomain(c, a)
        }

        val days = Time.lastNCalendarDaysStartsIncludingToday(7)
        val trend = days.map { dayStart ->
            DailySpendPoint(
                dayEpochMillis = dayStart,
                totalExpense = transactionDao.sumExpenseForDay(dayStart, dayStart + DAY_MS),
            )
        }

        return DashboardSummary(
            totalBalance = totalBalance,
            monthlyIncome = monthlyIncome,
            monthlySpend = monthlySpend,
            monthlyBudget = monthlyBudget,
            budgetProgress = budgetProgress,
            recentTransactions = recent,
            dailySpendLast7Days = trend,
        )
    }

    override fun observeDashboardSummary(): Flow<DashboardSummary> =
        combine(
            transactionDao.observeTransactionDashboardFingerprint(),
            accountDao.observeAccounts(),
            categoryDao.observeCategories(),
        ) { _, _, _ ->
            Unit
        }.mapLatest {
            getDashboardSummary()
        }

    override fun observeReceiptRows(): Flow<List<Transaction>> =
        transactionDao.observeWithImages().mapLatest { entities ->
            if (entities.isEmpty()) return@mapLatest emptyList()
            val cats = categoryDao.getAll().associateBy { it.id }
            val accs = accountDao.getAll().associateBy { it.id }
            entities.mapNotNull { e ->
                val c = cats[e.categoryId] ?: return@mapNotNull null
                val a = accs[e.accountId] ?: return@mapNotNull null
                e.toDomain(c, a)
            }
        }

    override suspend fun monthlyExpenseTotal(epochMillis: Long): Double {
        val start = Time.startOfMonthMillis(epochMillis)
        val end = Time.startOfNextMonthMillis(epochMillis)
        return transactionDao.sumExpenseBetween(start, end)
    }

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
