package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Generates a summary report of career metrics.
 */
class GenerateUsageReportUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) : UseCase<Unit, CoreResult<String>>() {

    override suspend operator fun invoke(input: Unit): CoreResult<String> = runCatchingCore {
        val snapshots = repository.getSnapshots().firstOrNull()?.getOrNull() ?: emptyList()
        val latest = snapshots.firstOrNull()

        buildString {
            appendLine("Career Usage Report")
            appendLine("Career Score: ${latest?.careerScore ?: 0}")
            appendLine("KPIs: ${latest?.kpis?.entries?.joinToString { "${it.key}: ${it.value}" }}")
        }
    }
}
