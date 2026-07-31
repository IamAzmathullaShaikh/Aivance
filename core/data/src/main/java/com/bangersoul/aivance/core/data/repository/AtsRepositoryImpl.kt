package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.JobDescription
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.domain.repository.AtsRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AtsRepositoryImpl @Inject constructor(
    private val atsDao: AtsDao,
    private val resumeDao: ResumeDao,
    private val providerManager: ProviderManager
) : AtsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getReportsForVersion(versionId: Long): Flow<CoreResult<List<AtsReport>>> {
        return atsDao.getReportsForVersion(versionId).map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun getReportById(id: Long): AtsReport? {
        return atsDao.getReportById(id)?.toDomain()
    }

    override suspend fun saveReport(report: AtsReport): CoreResult<Long> = runCatchingCore {
        atsDao.insertReport(report.toEntity())
    }

    override suspend fun deleteReport(id: Long): CoreResult<Unit> = runCatchingCore {
        val report = atsDao.getReportById(id) ?: throw Exception("Report not found")
        atsDao.deleteReport(report)
    }

    override suspend fun saveJobDescription(jd: JobDescription): CoreResult<Long> = runCatchingCore {
        atsDao.insertJobDescription(jd.toEntity())
    }

    override suspend fun getJobDescription(id: Long): JobDescription? {
        return atsDao.getJobDescriptionById(id)?.toDomain()
    }

    override suspend fun performAtsAnalysis(
        resumeId: Long,
        versionId: Long,
        jobDescriptionId: Long
    ): CoreResult<AtsReport> = runCatchingCore {
        val version = resumeDao.getVersionById(versionId) ?: throw Exception("Resume version not found")
        val sections = resumeDao.getSectionsForVersion(versionId).firstOrNull() ?: emptyList()
        val jd = atsDao.getJobDescriptionById(jobDescriptionId) ?: throw Exception("Job description not found")

        val resumeContent = sections.joinToString("\n\n") { "${it.title}:\n${it.content}" }
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Perform a detailed ATS (Applicant Tracking System) analysis by comparing the Resume against the Job Description.

            Resume:
            $resumeContent

            Job Description:
            ${jd.rawText}

            Return ONLY a structured JSON object with the following fields:
            "overallScore": Int (0-100),
            "matchPercentage": Int (0-100),
            "matchedKeywords": [String],
            "missingKeywords": [String],
            "sectionScores": { "Summary": Int, "Experience": Int, "Skills": Int, "Education": Int },
            "optimizationTips": [
                { "category": String, "description": String, "priority": "HIGH"|"MEDIUM"|"LOW" }
            ]
        """.trimIndent()

        val result = provider.generateText(prompt).getOrNull() ?: throw Exception("AI analysis failed")

        val jsonText = if (result.contains("```json")) {
            result.substringAfter("```json").substringBefore("```").trim()
        } else if (result.contains("{") && result.contains("}")) {
            result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1)
        } else result

        val report = json.decodeFromString<AtsReport>(jsonText).copy(
            resumeVersionId = versionId,
            jobDescriptionId = jobDescriptionId
        )

        val id = atsDao.insertReport(report.toEntity())
        report.copy(id = id)
    }
}
