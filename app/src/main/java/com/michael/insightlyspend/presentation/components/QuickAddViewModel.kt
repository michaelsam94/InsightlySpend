package com.michael.insightlyspend.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.Account
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.usecase.AddTransactionUseCase
import com.michael.insightlyspend.domain.repository.AccountRepository
import com.michael.insightlyspend.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
    private val addTransaction: AddTransactionUseCase,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        categoryRepository.observeCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val accounts: StateFlow<List<Account>> =
        accountRepository.observeAccounts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun saveTransaction(
        amount: Double,
        categoryId: Long,
        accountId: Long,
        note: String?,
        imagePath: String?,
        paymentMethod: PaymentMethod,
        isIncome: Boolean,
        debtDirection: DebtDirection,
        markRecurringTemplate: Boolean,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            addTransaction(
                amount = amount,
                timestamp = System.currentTimeMillis(),
                categoryId = categoryId,
                note = note,
                imagePath = imagePath,
                isRecurring = markRecurringTemplate,
                accountId = accountId,
                paymentMethod = paymentMethod,
                isIncome = isIncome,
                debtDirection = debtDirection,
            )
            onDone()
        }
    }
}
