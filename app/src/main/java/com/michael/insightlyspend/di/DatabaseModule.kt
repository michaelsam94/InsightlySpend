package com.michael.insightlyspend.di

import android.content.Context
import androidx.room.Room
import com.michael.insightlyspend.data.local.InsightlyDatabase
import com.michael.insightlyspend.data.local.InsightlyDatabaseMigrations
import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.local.dao.CategoryDao
import com.michael.insightlyspend.data.local.dao.RecurringRuleDao
import com.michael.insightlyspend.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InsightlyDatabase =
        Room.databaseBuilder(context, InsightlyDatabase::class.java, "insightly.db")
            .addCallback(InsightlyDatabase.SeedCallback())
            .addMigrations(InsightlyDatabaseMigrations.MIGRATION_1_2)
            .build()

    @Provides
    fun provideCategoryDao(db: InsightlyDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideAccountDao(db: InsightlyDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideTransactionDao(db: InsightlyDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideRecurringRuleDao(db: InsightlyDatabase): RecurringRuleDao = db.recurringRuleDao()
}
