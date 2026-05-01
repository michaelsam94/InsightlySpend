package com.michael.insightlyspend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michael.insightlyspend.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    /**
     * Emits whenever transaction rows change in a way that affects totals or lists.
     * Used as an invalidation signal for dashboard aggregation (avoids stale one-shot queries).
     */
    @Query(
        """
        SELECT
          CAST(COUNT(*) AS REAL) +
          IFNULL(SUM(amount), 0.0) +
          IFNULL(SUM(timestamp), 0.0) / 1e15 +
          IFNULL(SUM(categoryId), 0.0) +
          IFNULL(SUM(accountId), 0.0) +
          IFNULL(SUM(LENGTH(IFNULL(note, ''))), 0.0) +
          IFNULL(SUM(LENGTH(paymentMethod)), 0.0) +
          IFNULL(SUM(LENGTH(debtDirection)), 0.0) +
          IFNULL(SUM(LENGTH(IFNULL(imagePath, ''))), 0.0) +
          IFNULL(SUM(CASE WHEN isIncome != 0 THEN 1 ELSE 0 END), 0.0) +
          IFNULL(SUM(CASE WHEN isRecurring != 0 THEN 1 ELSE 0 END), 0.0)
        FROM transactions
        """,
    )
    fun observeTransactionDashboardFingerprint(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        UPDATE transactions SET categoryId = :newCategoryId
        WHERE categoryId = :oldCategoryId
        """,
    )
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)

    @Query(
        """
        SELECT * FROM transactions
        WHERE (:start IS NULL OR timestamp >= :start)
          AND (:end IS NULL OR timestamp <= :end)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
          AND (:minAmount IS NULL OR amount >= :minAmount)
          AND (:maxAmount IS NULL OR amount <= :maxAmount)
          AND (:debtDirection IS NULL OR debtDirection = :debtDirection)
          AND (:query = '' OR IFNULL(note, '') LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
        """,
    )
    fun observeFiltered(
        start: Long?,
        end: Long?,
        categoryId: Long?,
        paymentMethod: String?,
        minAmount: Double?,
        maxAmount: Double?,
        debtDirection: String?,
        query: String,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT IFNULL(SUM(amount), 0) FROM transactions
        WHERE isIncome = 0 AND timestamp >= :start AND timestamp < :end
        """,
    )
    suspend fun sumExpenseBetween(start: Long, end: Long): Double

    @Query(
        """
        SELECT IFNULL(SUM(amount), 0) FROM transactions
        WHERE isIncome = 1 AND timestamp >= :start AND timestamp < :end
        """,
    )
    suspend fun sumIncomeBetween(start: Long, end: Long): Double

    @Query(
        """
        SELECT IFNULL(SUM(amount), 0) FROM transactions
        WHERE isIncome = 0 AND categoryId = :categoryId AND timestamp >= :start AND timestamp < :end
        """,
    )
    suspend fun sumExpenseForCategoryBetween(categoryId: Long, start: Long, end: Long): Double

    @Query(
        """
        SELECT categoryId AS categoryId, IFNULL(SUM(amount), 0) AS total FROM transactions
        WHERE isIncome = 0 AND timestamp >= :start AND timestamp < :end
        GROUP BY categoryId
        """,
    )
    suspend fun sumExpenseByCategoryBetween(start: Long, end: Long): List<CategorySpendRow>

    @Query(
        """
        SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit
        """,
    )
    suspend fun getRecent(limit: Int): List<TransactionEntity>

    @Query(
        """
        SELECT IFNULL(SUM(amount), 0) FROM transactions
        WHERE isIncome = 0 AND timestamp >= :dayStart AND timestamp < :dayEnd
        """,
    )
    suspend fun sumExpenseForDay(dayStart: Long, dayEnd: Long): Double

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions WHERE imagePath IS NOT NULL ORDER BY timestamp DESC
        """,
    )
    fun observeWithImages(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE isIncome = 0 AND timestamp >= :start AND timestamp < :end
        ORDER BY timestamp ASC
        """,
    )
    suspend fun getExpensesBetween(start: Long, end: Long): List<TransactionEntity>
}

data class CategorySpendRow(
    val categoryId: Long,
    val total: Double,
)
