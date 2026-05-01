package com.michael.insightlyspend.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.RecurringRuleDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import com.michael.insightlyspend.data.local.entity.AccountEntity
import com.michael.insightlyspend.data.local.entity.CategoryEntity
import com.michael.insightlyspend.data.local.entity.RecurringRuleEntity
import com.michael.insightlyspend.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        RecurringRuleEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class InsightlyDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.beginTransaction()
            try {
                db.execSQL(
                    """
                    INSERT INTO accounts (id, accountName, currentBalance)
                    VALUES (1, 'Personal Wallet', 0.0);
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO accounts (id, accountName, currentBalance)
                    VALUES (2, 'Business', 0.0);
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO categories (id, name, nameEn, nameAr, iconResource, budgetLimit, rolloverUnusedToNextMonth)
                    VALUES
                      (1, 'Housing', 'Housing', NULL, 'home', 1200.0, 0),
                      (2, 'Food & Dining', 'Food & Dining', NULL, 'restaurant', 400.0, 0),
                      (3, 'Transport', 'Transport', NULL, 'directions_car', 200.0, 0),
                      (4, 'Entertainment', 'Entertainment', NULL, 'movie', 150.0, 0),
                      (5, 'Utilities', 'Utilities', NULL, 'bolt', 250.0, 0),
                      (6, 'Income', 'Income', NULL, 'payments', NULL, 0);
                    """.trimIndent(),
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
