package com.michael.insightlyspend.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.domain.notifications.BudgetAlertNotifier
import com.michael.insightlyspend.domain.repository.CategoryRepository
import com.michael.insightlyspend.domain.usecase.CalculateBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BudgetUiState {
    data object Loading : BudgetUiState
    data class Ready(val rows: List<BudgetCategoryStatus>) : BudgetUiState
    data class Error(val message: String) : BudgetUiState
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val calculateBudget: CalculateBudgetUseCase,
    private val categoryRepository: CategoryRepository,
    private val budgetNotifier: BudgetAlertNotifier,
) : ViewModel() {

    private val _state = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val state: StateFlow<BudgetUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = BudgetUiState.Loading
            try {
                val rows = calculateBudget()
                budgetNotifier.notifyThresholdCrossings(rows)
                _state.value = BudgetUiState.Ready(rows)
            } catch (t: Throwable) {
                _state.value = BudgetUiState.Error(t.message ?: "Unable to load budgets")
            }
        }
    }

    /** Creates a custom category with a monthly budget so it appears on this screen. */
    fun createCustomBudgetCategory(nameEn: String?, nameAr: String?, monthlyBudget: Double) {
        viewModelScope.launch {
            try {
                categoryRepository.createCategory(nameEn, nameAr, monthlyBudgetLimit = monthlyBudget)
                refresh()
            } catch (t: Throwable) {
                _state.value = BudgetUiState.Error(t.message ?: "Unable to create category")
            }
        }
    }

    fun updateBudgetCategory(categoryId: Long, nameEn: String?, nameAr: String?, monthlyBudget: Double) {
        viewModelScope.launch {
            try {
                val existing = categoryRepository.getCategory(categoryId) ?: return@launch
                categoryRepository.updateCategory(categoryId, nameEn, nameAr)
                categoryRepository.setBudget(
                    categoryId,
                    monthlyBudget,
                    existing.rolloverUnusedToNextMonth,
                )
                refresh()
            } catch (t: Throwable) {
                _state.value = BudgetUiState.Error(t.message ?: "Unable to update category")
            }
        }
    }
}
