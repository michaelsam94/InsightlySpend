package com.michael.insightlyspend.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.DashboardSummary
import com.michael.insightlyspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Ready(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
) : ViewModel() {

    /**
     * Driven by Room flows ([TransactionRepository.observeDashboardSummary]): any transaction,
     * account, or category change recomputes the Home overview. Uses [SharingStarted.Eagerly] so
     * updates keep flowing while this VM exists (e.g. background recurring worker inserts a row).
     */
    val state: StateFlow<DashboardUiState> =
        transactionRepository.observeDashboardSummary()
            .map<DashboardSummary, DashboardUiState> { summary -> DashboardUiState.Ready(summary) }
            .catch { t ->
                emit(DashboardUiState.Error(t.message ?: "Unable to load dashboard"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = DashboardUiState.Loading,
            )
}
