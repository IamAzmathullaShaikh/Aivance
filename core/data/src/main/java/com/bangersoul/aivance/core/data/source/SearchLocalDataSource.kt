package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.SearchResult
import com.bangersoul.aivance.core.database.dao.SearchDao
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

interface SearchLocalDataSource {
    fun getSavedSearches(): Flow<List<SearchResult>>
    suspend fun saveSearch(search: SearchResult)
    suspend fun deleteSearch(id: Long)
}

class SearchLocalDataSourceImpl @Inject constructor(
    private val searchDao: SearchDao
) : SearchLocalDataSource {

    override fun getSavedSearches(): Flow<List<SearchResult>> {
        return searchDao.getSavedSearches().map { entities ->
            entities.map { entity ->
                SearchResult(
                    id = entity.id.toString(),
                    query = entity.query,
                    totalResults = 0,
                    page = 0,
                    items = emptyList(),
                    timestamp = entity.dateCreated.toEpochMilli()
                )
            }
        }
    }

    override suspend fun saveSearch(search: SearchResult) {
        searchDao.insertSavedSearch(
            SavedSearchEntity(
                query = search.query,
                filters = emptyMap(), // SearchResult doesn't have filters
                dateCreated = Instant.ofEpochMilli(search.timestamp)
            )
        )
    }

    override suspend fun deleteSearch(id: Long) {
        searchDao.deleteSavedSearchById(id)
    }
}
