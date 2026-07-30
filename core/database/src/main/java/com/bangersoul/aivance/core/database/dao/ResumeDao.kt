package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.ResumeEntity
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resumes ORDER BY lastModified DESC")
    fun getResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: Long): ResumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResumes(resumes: List<ResumeEntity>)

    @Delete
    suspend fun deleteResume(resume: ResumeEntity)

    @Query("SELECT * FROM resume_sections WHERE resumeId = :resumeId ORDER BY sectionOrder ASC")
    fun getSectionsForResume(resumeId: Long): Flow<List<ResumeSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<ResumeSectionEntity>)

    @Query("DELETE FROM resume_sections WHERE resumeId = :resumeId")
    suspend fun deleteSectionsForResume(resumeId: Long)

    @Transaction
    suspend fun updateResumeWithSections(resume: ResumeEntity, sections: List<ResumeSectionEntity>) {
        insertResume(resume)
        deleteSectionsForResume(resume.id)
        insertSections(sections)
    }
}
