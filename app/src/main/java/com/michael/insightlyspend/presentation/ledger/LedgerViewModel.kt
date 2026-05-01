package com.michael.insightlyspend.presentation.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.LedgerFilters
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.Transaction
import com.michael.insightlyspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(LedgerFilters())
    private val searchQuery = MutableStateFlow("")

    val ledger: StateFlow<List<Transaction>> =
        combine(filters, searchQuery) { f, q -> f.copy(query = q) }
            .flatMapLatest { transactionRepository.observeLedger(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSearch(q: String) {
        searchQuery.value = q
    }

    fun setDateRange(start: Long?, end: Long?) {
        filters.value = filters.value.copy(startDate = start, endDate = end)
    }

    fun setCategory(id: Long?) {
        filters.value = filters.value.copy(categoryId = id)
    }

    fun setPayment(method: PaymentMethod?) {
        filters.value = filters.value.copy(paymentMethod = method)
    }

    fun delete(id: Long) {
        viewModelScope.launch { transactionRepository.deleteTransaction(id) }
    }

    fun duplicate(id: Long) {
        viewModelScope.launch { transactionRepository.duplicateTransaction(id) }
    }
}
