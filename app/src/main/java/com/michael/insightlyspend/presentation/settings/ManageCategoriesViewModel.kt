package com.michael.insightlyspend.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.insightlyspend.domain.model.Category
import com.michael.insightlyspend.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        categoryRepository.observeCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    fun createCategory(nameEn: String?, nameAr: String?) {
        viewModelScope.launch {
            categoryRepository.createCategory(nameEn, nameAr, monthlyBudgetLimit = null)
        }
    }

    fun updateCategory(categoryId: Long, nameEn: String?, nameAr: String?) {
        viewModelScope.launch {
            categoryRepository.updateCategory(categoryId, nameEn, nameAr)
        }
    }

    fun deleteCategory(categoryId: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = categoryRepository.deleteCategory(categoryId)
            onDone(ok)
        }
    }
}
