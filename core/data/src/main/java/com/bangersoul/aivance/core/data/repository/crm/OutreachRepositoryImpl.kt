package com.bangersoul.aivance.core.data.repository.crm

import com.bangersoul.aivance.core.common.model.OutreachDraft
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.RecruiterDao
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.repository.crm.OutreachRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutreachRepositoryImpl @Inject constructor(
    private val recruiterDao: RecruiterDao,
    private val resumeRepository: ResumeRepository,
    private val providerManager: ProviderManager
) : OutreachRepository {

    override fun getDraftsForRecruiter(recruiterId: String): Flow<CoreResult<List<OutreachDraft>>> {
        return recruiterDao.getDraftsForRecruiter(recruiterId).map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun generateDraft(
        resumeId: Long,
        versionId: Long,
        recruiterId: String,
        jobId: String,
        type: String
    ): CoreResult<OutreachDraft> = runCatchingCore {
        val recruiter = recruiterDao.getRecruiterById(recruiterId) ?: throw Exception("Recruiter not found")
        val versions = resumeRepository.getVersions(resumeId).firstOrNull()?.getOrNull() ?: emptyList()
        val version = versions.find { it.id == versionId } ?: throw Exception("Resume version not found")

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Generate a $type message for recruiter ${recruiter.name} at company.
            Resume Context: ${version.sections.joinToString { it.title }}
            Role Context: $jobId
        """.trimIndent()

        val content = provider.generateText(prompt).getOrNull() ?: throw Exception("AI generation failed")

        val draft = OutreachDraft(
            recruiterId = recruiterId,
            jobId = jobId,
            type = type,
            content = content
        )
        val id = saveDraft(draft).getOrNull() ?: 0L
        draft.copy(id = id)
    }

    override suspend fun saveDraft(draft: OutreachDraft): CoreResult<Long> = runCatchingCore {
        recruiterDao.insertDraft(draft.toEntity())
    }
}
