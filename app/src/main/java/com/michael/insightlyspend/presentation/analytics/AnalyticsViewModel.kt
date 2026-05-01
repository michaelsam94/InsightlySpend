package com.michael.insightlyspend.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.FullMlInsightReport
import com.michael.insightlyspend.domain.repository.InsightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data class Ready(val report: FullMlInsightReport) : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val insightsRepository: InsightsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AnalyticsUiState.Loading
            try {
                val report = insightsRepository.buildReport(System.currentTimeMillis())
                _state.value = AnalyticsUiState.Ready(report)
            } catch (t: Throwable) {
                _state.value = AnalyticsUiState.Error(t.message ?: "Unable to compute insights")
            }
        }
    }
}
