package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.SearchResult
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getSavedSearches(): Flow<CoreResult<List<SearchResult>>>
    suspend fun saveSearch(search: SearchResult): CoreResult<Unit>
    suspend fun deleteSearch(id: String): CoreResult<Unit>
}
