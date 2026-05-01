package com.michael.insightlyspend.presentation.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.Transaction
import com.michael.insightlyspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
) : ViewModel() {

    val receipts: StateFlow<List<Transaction>> =
        transactionRepository.observeReceiptRows().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )
}
