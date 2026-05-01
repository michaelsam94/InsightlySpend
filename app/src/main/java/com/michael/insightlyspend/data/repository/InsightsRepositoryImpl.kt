package com.michael.insightlyspend.data.repository

import com.michael.insightlyspend.data.ml.LocalMlInsightsEngine
import com.michael.insightlyspend.domain.model.FullMlInsightReport
import com.michael.insightlyspend.domain.repository.InsightsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsRepositoryImpl @Inject constructor(
    private val engine: LocalMlInsightsEngine,
) : InsightsRepository {
    override suspend fun buildReport(nowEpochMillis: Long): FullMlInsightReport =
        engine.buildReport(nowEpochMillis)
}
