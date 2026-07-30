package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Query("SELECT * FROM saved_searches ORDER BY dateCreated DESC")
    fun getSavedSearches(): Flow<List<SavedSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSearch(search: SavedSearchEntity): Long

    @Delete
    suspend fun deleteSavedSearch(search: SavedSearchEntity)

    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun deleteSavedSearchById(id: Long)

    @Query("DELETE FROM saved_searches")
    suspend fun deleteAllSavedSearches()
}
