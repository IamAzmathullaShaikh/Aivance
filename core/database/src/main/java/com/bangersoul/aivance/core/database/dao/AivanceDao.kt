package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.AivanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AivanceDao {
    @Query("SELECT * FROM aivance_entities")
    fun getAllEntities(): Flow<List<AivanceEntity>>

    @Query("SELECT * FROM aivance_entities WHERE id = :id")
    suspend fun getEntityById(id: Int): AivanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: AivanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities(entities: List<AivanceEntity>)

    @Delete
    suspend fun deleteEntity(entity: AivanceEntity)
}
