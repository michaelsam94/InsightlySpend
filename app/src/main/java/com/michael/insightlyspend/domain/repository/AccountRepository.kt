package com.michael.insightlyspend.domain.repository

import com.michael.insightlyspend.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
}
