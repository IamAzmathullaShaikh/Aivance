package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun logEvent(event: AnalyticsEvent): CoreResult<Unit>
    fun getEvents(category: String? = null): Flow<CoreResult<List<AnalyticsEvent>>>
}
