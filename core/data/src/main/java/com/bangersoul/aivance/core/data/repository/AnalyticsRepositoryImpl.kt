package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.source.AiLocalDataSource
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val localDataSource: AiLocalDataSource
) : AnalyticsRepository {

    override suspend fun logEvent(event: AnalyticsEvent): CoreResult<Unit> = runCatchingCore {
        localDataSource.saveAnalyticsEvent(event)
    }

    override fun getEvents(category: String?): Flow<CoreResult<List<AnalyticsEvent>>> {
        return localDataSource.getAnalyticsEvents().map { events ->
            runCatchingCore {
                if (category != null) events.filter { it.category == category } else events
            }
        }
    }
}
