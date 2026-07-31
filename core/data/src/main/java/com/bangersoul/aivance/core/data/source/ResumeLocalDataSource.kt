package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ResumeLocalDataSource {
    fun getResumes(): Flow<List<Resume>>
    suspend fun getResumeById(id: Long): Resume?
    suspend fun saveResume(resume: Resume): Long
    suspend fun deleteResume(resume: Resume)

    fun getVersionsForResume(resumeId: Long): Flow<List<ResumeVersion>>
    suspend fun saveVersion(version: ResumeVersion): Long
    suspend fun deleteVersion(version: ResumeVersion)

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
        val versions = resumeDao.getVersionsForResume(id).first().map { vEntity ->
            val sections = resumeDao.getSectionsForVersion(vEntity.id).first()
            vEntity.toDomain(sections)
        }
        return entity.toDomain(versions)
    }

    override suspend fun saveResume(resume: Resume): Long {
        return resumeDao.insertResume(resume.toEntity())
    }

    override suspend fun deleteResume(resume: Resume) {
        resumeDao.deleteResume(resume.toEntity())
    }

    override fun getVersionsForResume(resumeId: Long): Flow<List<ResumeVersion>> {
        return resumeDao.getVersionsForResume(resumeId).map { versions ->
            versions.map { v ->
                // This is a bit inefficient for a stream, better to use @Relation in Room
                // but for now keeping it simple.
                v.toDomain()
            }
        }
    }

    override suspend fun saveVersion(version: ResumeVersion): Long {
        val entity = version.toEntity()
        val sections = version.sections.map { it.toEntity(version.id) }
        resumeDao.updateVersionWithSections(entity, sections)
        return entity.id
    }

    override suspend fun deleteVersion(version: ResumeVersion) {
        resumeDao.deleteVersion(version.toEntity())
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
        val entity = com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity(
            id = atsResult.id,
            resumeId = resumeId,
            jobDescription = "",
            score = atsResult.score,
            matchedKeywords = atsResult.matchingKeywords.joinToString(","),
            missingKeywords = atsResult.missingKeywords.joinToString(","),
            feedback = atsResult.feedback,
            date = atsResult.date
        )
        atsDao.insertAtsResult(entity)
    }
}
