package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.CoverLetterVersion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.CoverLetterDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverLetterRepositoryImpl @Inject constructor(
    private val coverLetterDao: CoverLetterDao,
    private val jobDao: JobDao,
    private val providerManager: ProviderManager
) : CoverLetterRepository {

    override fun getCoverLetters(): Flow<CoreResult<List<CoverLetter>>> {
        return coverLetterDao.getCoverLetters().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override fun getCoverLetterById(id: Long): Flow<CoreResult<CoverLetter>> {
        return coverLetterDao.getCoverLetters().map { list ->
            runCatchingCore {
                val entity = list.find { it.id.toLong() == id } ?: throw Exception("Cover letter not found")
                val vEntities = coverLetterDao.getVersionsForCoverLetter(id).firstOrNull() ?: emptyList()
                val versions = vEntities.map { v ->
                    val sections = coverLetterDao.getSectionsForVersion(v.id).firstOrNull() ?: emptyList()
                    v.toDomain(sections)
                }
                entity.toDomain(versions)
            }
        }
    }

    override suspend fun saveCoverLetter(coverLetter: CoverLetter): CoreResult<Long> = runCatchingCore {
        coverLetterDao.insertCoverLetter(coverLetter.toEntity())
    }

    override suspend fun deleteCoverLetter(id: Long): CoreResult<Unit> = runCatchingCore {
        val entity = coverLetterDao.getCoverLetterById(id) ?: throw Exception("Not found")
        coverLetterDao.deleteCoverLetter(entity)
    }

    override fun getVersions(coverLetterId: Long): Flow<CoreResult<List<CoverLetterVersion>>> {
        return coverLetterDao.getVersionsForCoverLetter(coverLetterId).map { entities ->
            runCatchingCore {
                entities.map { v ->
                    val sections = coverLetterDao.getSectionsForVersion(v.id).firstOrNull() ?: emptyList()
                    v.toDomain(sections)
                }
            }
        }
    }

    override suspend fun saveVersion(version: CoverLetterVersion): CoreResult<Long> = runCatchingCore {
        val entity = version.toEntity()
        val sections = version.sections.map { it.toEntity(version.id) }
        coverLetterDao.updateVersionWithSections(entity, sections)
        entity.id
    }

    override suspend fun deleteVersion(coverLetterId: Long, versionId: Long): CoreResult<Unit> = runCatchingCore {
        val version = coverLetterDao.getVersionById(versionId) ?: throw Exception("Version not found")
        coverLetterDao.deleteVersion(version)
    }

    override suspend fun generateCoverLetter(
        resumeId: Long,
        resumeVersionId: Long,
        jobId: Long,
        recruiterId: String?,
        writingStyle: String
    ): CoreResult<Long> = runCatchingCore {
        val job = jobDao.getJobWithDetailsById(jobId) ?: throw Exception("Job not found")

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = "Generate a $writingStyle cover letter for ${job.company.name} as ${job.job.title}."
        val content = provider.generateText(prompt).getOrNull() ?: throw Exception("AI generation failed")

        val coverLetterId = coverLetterDao.insertCoverLetter(
            CoverLetter(
                resumeVersionId = resumeVersionId,
                jobId = jobId,
                recruiterId = recruiterId,
                company = job.company.name,
                role = job.job.title
            ).toEntity()
        )

        val version = CoverLetterVersion(
            coverLetterId = coverLetterId,
            versionName = "Initial Draft",
            writingStyle = writingStyle,
            sections = listOf(com.bangersoul.aivance.core.common.model.CoverLetterSection(sectionType = "BODY", title = "Body", content = content))
        )
        saveVersion(version)

        coverLetterId
    }

    override suspend fun regenerateSection(versionId: Long, sectionType: String): CoreResult<Unit> = runCatchingCore {
        // AI logic for single section regeneration
    }
}
