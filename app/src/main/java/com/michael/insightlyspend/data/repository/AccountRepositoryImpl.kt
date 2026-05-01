package com.michael.insightlyspend.data.repository

import com.michael.insightlyspend.data.local.dao.AccountDao
import com.michael.insightlyspend.data.mapper.toDomain
import com.michael.insightlyspend.domain.model.Account
import com.michael.insightlyspend.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        accountDao.observeAccounts().map { list -> list.map { it.toDomain() } }
}
