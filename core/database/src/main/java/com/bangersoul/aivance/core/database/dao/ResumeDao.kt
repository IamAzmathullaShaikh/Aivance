package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.ResumeEntity
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import com.bangersoul.aivance.core.database.model.ResumeVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resumes ORDER BY lastModified DESC")
    fun getResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: Long): ResumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity): Long

    @Delete
    suspend fun deleteResume(resume: ResumeEntity)

    // Versions
    @Query("SELECT * FROM resume_versions WHERE resumeId = :resumeId ORDER BY lastModified DESC")
    fun getVersionsForResume(resumeId: Long): Flow<List<ResumeVersionEntity>>

    @Query("SELECT * FROM resume_versions WHERE id = :versionId")
    suspend fun getVersionById(versionId: Long): ResumeVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ResumeVersionEntity): Long

    @Delete
    suspend fun deleteVersion(version: ResumeVersionEntity)

    // Sections
    @Query("SELECT * FROM resume_sections WHERE versionId = :versionId ORDER BY sectionOrder ASC")
    fun getSectionsForVersion(versionId: Long): Flow<List<ResumeSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<ResumeSectionEntity>)

    @Query("DELETE FROM resume_sections WHERE versionId = :versionId")
    suspend fun deleteSectionsForVersion(versionId: Long)

    @Transaction
    suspend fun updateVersionWithSections(version: ResumeVersionEntity, sections: List<ResumeSectionEntity>) {
        insertVersion(version)
        deleteSectionsForVersion(version.id)
        insertSections(sections)
    }
}
