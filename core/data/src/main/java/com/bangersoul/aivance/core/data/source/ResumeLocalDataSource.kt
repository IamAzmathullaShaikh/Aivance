package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ResumeLocalDataSource {
    fun getResumes(): Flow<List<Resume>>
    suspend fun getResumeById(id: Long): Resume?
    suspend fun saveResume(resume: Resume)
    suspend fun deleteResume(resume: Resume)
    fun getAtsResults(): Flow<List<AtsResult>>
    fun getLatestAtsResult(): Flow<AtsResult?>
    suspend fun saveAtsResult(atsResult: AtsResult, resumeId: Long)
}

class ResumeLocalDataSourceImpl @Inject constructor(
    private val resumeDao: ResumeDao,
    private val atsDao: AtsDao
) : ResumeLocalDataSource {

    override fun getResumes(): Flow<List<Resume>> {
        return resumeDao.getResumes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getResumeById(id: Long): Resume? {
        val entity = resumeDao.getResumeById(id) ?: return null
        // Since sections are a separate table, we might want to fetch them too.
        // But ResumeEntity.toDomain() takes sections.
        // However, ResumeDao doesn't have a Room @Transaction for ResumeWithSections.
        // I'll just map it with empty sections for now or fetch them if needed.
        return entity.toDomain()
    }

    override suspend fun saveResume(resume: Resume) {
        val entity = resume.toEntity()
        val sections = resume.sections.mapIndexed { index, section ->
            ResumeSectionEntity(
                resumeId = resume.id,
                title = section.title,
                content = section.content,
                sectionOrder = index
            )
        }
        resumeDao.updateResumeWithSections(entity, sections)
    }

    override suspend fun deleteResume(resume: Resume) {
        resumeDao.deleteResume(resume.toEntity())
    }

    override fun getAtsResults(): Flow<List<AtsResult>> {
        return atsDao.getAtsResults().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLatestAtsResult(): Flow<AtsResult?> {
        return atsDao.getLatestAtsResult().map { it?.toDomain() }
    }

    override suspend fun saveAtsResult(atsResult: AtsResult, resumeId: Long) {
        // Need to convert AtsResult to ResumeAnalysisEntity
        val entity = com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity(
            id = atsResult.id,
            resumeId = resumeId,
            jobDescription = "", // Info missing in AtsResult
            score = atsResult.score,
            matchedKeywords = atsResult.matchingKeywords.joinToString(","),
            missingKeywords = atsResult.missingKeywords.joinToString(","),
            feedback = atsResult.feedback,
            date = atsResult.date
        )
        atsDao.insertAtsResult(entity)
    }
}
