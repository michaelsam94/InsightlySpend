package com.michael.insightlyspend.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.michael.insightlyspend.data.work.RecurringTransactionWorker
import com.michael.insightlyspend.domain.model.DebtDirection
import com.michael.insightlyspend.domain.model.PaymentMethod
import com.michael.insightlyspend.domain.model.RecurringRule
import com.michael.insightlyspend.domain.repository.RecurringRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringRulesViewModel @Inject constructor(
    private val repository: RecurringRuleRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    /** [SharingStarted.Eagerly]: keep observing Room while Settings exists so saves reflect immediately. */
    val rules: StateFlow<List<RecurringRule>> =
        repository.observeRecurringRules().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    fun upsert(
        id: Long?,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        note: String?,
        paymentMethod: PaymentMethod,
        isIncome: Boolean,
        debtDirection: DebtDirection,
        dayOfMonth: Int,
    ) {
        viewModelScope.launch {
            repository.upsert(
                id = id,
                amount = amount,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                paymentMethod = paymentMethod,
                isIncome = isIncome,
                debtDirection = debtDirection,
                dayOfMonth = dayOfMonth,
            )
            // Periodic work may not run for hours; post matching rules today ASAP so Home/Ledger update.
            enqueueRecurringWorkerOnce()
        }
    }

    private fun enqueueRecurringWorkerOnce() {
        val req = OneTimeWorkRequestBuilder<RecurringTransactionWorker>().build()
        WorkManager.getInstance(appContext).enqueue(req)
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
