package com.michael.insightlyspend.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.Account
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.domain.repository.AccountRepository
import com.michael.insightlyspend.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class QuickCategoriesAccountsViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        categoryRepository.observeCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val accounts: StateFlow<List<Account>> =
        accountRepository.observeAccounts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )
}
