package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.source.ResumeLocalDataSource
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ResumeRepositoryImpl @Inject constructor(
    private val localDataSource: ResumeLocalDataSource,
    private val providerManager: ProviderManager
) : ResumeRepository {

    override fun getResumes(): Flow<CoreResult<List<Resume>>> {
        return localDataSource.getResumes().map { runCatchingCore { it } }
    }

    override fun getResumeById(id: Long): Flow<CoreResult<Resume>> {
        return localDataSource.getResumes().map { resumes ->
            runCatchingCore { resumes.find { it.id == id } ?: throw Exception("Resume not found") }
        }
    }

    override suspend fun insertResume(resume: Resume): CoreResult<Long> = runCatchingCore {
        localDataSource.saveResume(resume)
        resume.id
    }

    override suspend fun updateResume(resume: Resume): CoreResult<Unit> = runCatchingCore {
        localDataSource.saveResume(resume)
    }

    override suspend fun deleteResume(id: Long): CoreResult<Unit> = runCatchingCore {
        val resume = localDataSource.getResumeById(id) ?: throw Exception("Resume not found")
        localDataSource.deleteResume(resume)
    }

    override suspend fun updateSections(resumeId: Long, sections: List<ResumeSection>): CoreResult<Unit> = runCatchingCore {
        val resume = localDataSource.getResumeById(resumeId) ?: throw Exception("Resume not found")
        localDataSource.saveResume(resume.copy(sections = sections))
    }

    override suspend fun analyzeResume(resumeId: Long, jobDescription: String): CoreResult<ResumeAnalysis> = runCatchingCore {
        val resume = localDataSource.getResumeById(resumeId) ?: throw Exception("Resume not found")
        val prompt = "Analyze this resume against the job description: \nResume: ${resume.rawText}\nJob: $jobDescription"
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? com.bangersoul.aivance.sdk.api.AIProvider
            ?: throw Exception("No AI provider available")
        val result = provider.generateText(prompt).getOrNull() ?: throw Exception("AI analysis failed")
        ResumeAnalysis(
            overallScore = 80,
            matchSummary = result
        )
    }

    override fun getAtsResults(resumeId: Long): Flow<CoreResult<List<AtsResult>>> {
        return localDataSource.getAtsResults().map { runCatchingCore { it } }
    }
}
