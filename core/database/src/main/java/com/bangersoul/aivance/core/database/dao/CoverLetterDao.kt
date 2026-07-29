package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.CoverLetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoverLetterDao {
    @Query("SELECT * FROM cover_letters ORDER BY dateCreated DESC")
    fun getCoverLetters(): Flow<List<CoverLetterEntity>>

    @Query("SELECT * FROM cover_letters WHERE id = :id")
    suspend fun getCoverLetterById(id: Int): CoverLetterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoverLetter(coverLetter: CoverLetterEntity)

    @Delete
    suspend fun deleteCoverLetter(coverLetter: CoverLetterEntity)

    @Query("DELETE FROM cover_letters WHERE id = :id")
    suspend fun deleteCoverLetterById(id: Int)
}
