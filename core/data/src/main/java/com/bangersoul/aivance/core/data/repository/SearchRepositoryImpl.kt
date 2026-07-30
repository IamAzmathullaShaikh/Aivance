package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.SearchResult
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.source.SearchLocalDataSource
import com.bangersoul.aivance.core.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val localDataSource: SearchLocalDataSource
) : SearchRepository {

    override fun getSavedSearches(): Flow<CoreResult<List<SearchResult>>> {
        return localDataSource.getSavedSearches().map { runCatchingCore { it } }
    }

    override suspend fun saveSearch(search: SearchResult): CoreResult<Unit> = runCatchingCore {
        localDataSource.saveSearch(search)
    }

    override suspend fun deleteSearch(id: String): CoreResult<Unit> = runCatchingCore {
        localDataSource.deleteSearch(id.toLongOrNull() ?: 0L)
    }
}
