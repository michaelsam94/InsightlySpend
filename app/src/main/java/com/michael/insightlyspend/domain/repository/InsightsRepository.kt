package com.michael.insightlyspend.domain.repository

import com.michael.insightlyspend.domain.model.FullMlInsightReport

interface InsightsRepository {
    suspend fun buildReport(nowEpochMillis: Long = System.currentTimeMillis()): FullMlInsightReport
}
