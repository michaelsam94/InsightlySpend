package com.michael.insightlyspend.domain.usecase

import com.michael.insightlyspend.domain.model.BudgetCategoryStatus
import com.michael.insightlyspend.domain.repository.CategoryRepository
import javax.inject.Inject

class CalculateBudgetUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(): List<BudgetCategoryStatus> =
        categoryRepository.budgetStatusesForMonth()
}
