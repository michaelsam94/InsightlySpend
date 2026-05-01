package com.michael.insightlyspend.di

import com.michael.insightlyspend.data.repository.AccountRepositoryImpl
import com.michael.insightlyspend.data.repository.CategoryRepositoryImpl
import com.michael.insightlyspend.data.repository.InsightsRepositoryImpl
import com.michael.insightlyspend.data.repository.RecurringRuleRepositoryImpl
import com.michael.insightlyspend.data.repository.TransactionRepositoryImpl
import com.michael.insightlyspend.data.repository.UserPreferencesRepositoryImpl
import com.michael.insightlyspend.domain.repository.AccountRepository
import com.michael.insightlyspend.domain.repository.CategoryRepository
import com.michael.insightlyspend.domain.repository.InsightsRepository
import com.michael.insightlyspend.domain.repository.RecurringRuleRepository
import com.michael.insightlyspend.domain.repository.TransactionRepository
import com.michael.insightlyspend.data.notifications.BudgetThresholdNotifier
import com.michael.insightlyspend.domain.notifications.BudgetAlertNotifier
import com.michael.insightlyspend.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository

    @Binds
    @Singleton
    abstract fun bindRecurringRuleRepository(impl: RecurringRuleRepositoryImpl): RecurringRuleRepository

    @Binds
    @Singleton
    abstract fun bindBudgetAlertNotifier(impl: BudgetThresholdNotifier): BudgetAlertNotifier
}
