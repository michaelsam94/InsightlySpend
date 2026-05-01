package com.michael.insightlyspend.domain.notifications

import com.michael.insightlyspend.domain.model.BudgetCategoryStatus

interface BudgetAlertNotifier {
    suspend fun notifyThresholdCrossings(rows: List<BudgetCategoryStatus>)
}
