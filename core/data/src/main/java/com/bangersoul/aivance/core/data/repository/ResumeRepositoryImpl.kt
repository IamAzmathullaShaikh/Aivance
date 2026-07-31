package com.bangersoul.aivance.core.data.repository

import android.content.Context
import android.net.Uri
import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.resume.ResumeParser
import com.bangersoul.aivance.core.data.source.ResumeLocalDataSource
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDataSource: ResumeLocalDataSource,
    private val providerManager: ProviderManager,
    private val resumeParser: ResumeParser
) : ResumeRepository {

    override fun getResumes(): Flow<CoreResult<List<Resume>>> {
        return localDataSource.getResumes().map { runCatchingCore { it } }
    }

    override fun getResumeById(id: Long): Flow<CoreResult<Resume>> {
        return localDataSource.getResumes().map { resumes ->
            runCatchingCore { resumes.find { it.id == id } ?: throw Exception("Resume not found") }
        }
    }

    override suspend fun saveResume(resume: Resume): CoreResult<Long> = runCatchingCore {
        localDataSource.saveResume(resume)
    }

    override suspend fun deleteResume(id: Long): CoreResult<Unit> = runCatchingCore {
        val resume = localDataSource.getResumeById(id) ?: throw Exception("Resume not found")
        localDataSource.deleteResume(resume)
    }

    override fun getVersions(resumeId: Long): Flow<CoreResult<List<ResumeVersion>>> {
        return localDataSource.getVersionsForResume(resumeId).map { runCatchingCore { it } }
    }

    override suspend fun saveVersion(version: ResumeVersion): CoreResult<Long> = runCatchingCore {
        localDataSource.saveVersion(version)
    }

    override suspend fun deleteVersion(resumeId: Long, versionId: Long): CoreResult<Unit> = runCatchingCore {
        val versions = localDataSource.getVersionsForResume(resumeId).firstOrNull() ?: emptyList()
        val version = versions.find { it.id == versionId } ?: throw Exception("Version not found")
        localDataSource.deleteVersion(version)
    }

    override suspend fun importResume(uri: Uri): CoreResult<Long> = runCatchingCore {
        val fileName = uri.lastPathSegment ?: "Imported Resume"
        val extension = fileName.substringAfterLast(".", "").lowercase()

        val extractedText: String = when (extension) {
            "pdf" -> com.bangersoul.aivance.core.util.PdfTextExtractor.extractTextFromPdf(context, uri)
            "docx" -> com.bangersoul.aivance.core.util.DocxTextExtractor.extractTextFromDocx(context, uri)
            else -> throw Exception("Unsupported file format: $extension")
        }

        if (extractedText.startsWith("Error extracting text")) {
            throw Exception(extractedText)
        }

        val resume = Resume(
            name = fileName.substringBeforeLast("."),
            fileName = fileName,
            fileUri = uri.toString(),
            rawText = extractedText
        )
        val id = localDataSource.saveResume(resume)

        // Trigger initial parsing
        parseResume(id)

        id
    }

    override suspend fun parseResume(resumeId: Long): CoreResult<Unit> = runCatchingCore {
        val resume = localDataSource.getResumeById(resumeId) ?: throw Exception("Resume not found")
        val rawText = resume.rawText ?: throw Exception("No text to parse")

        val sections = resumeParser.parseRawText(rawText)

        // Create initial version
        val version = ResumeVersion(
            resumeId = resumeId,
            versionName = "Original Import",
            sections = sections
        )
        val versionId = localDataSource.saveVersion(version)

        // Mark as primary
        localDataSource.saveResume(resume.copy(primaryVersionId = versionId))
    }

    override suspend fun analyzeResume(resumeId: Long, versionId: Long, jobDescription: String): CoreResult<ResumeAnalysis> = runCatchingCore {
        val versions = localDataSource.getVersionsForResume(resumeId).firstOrNull() ?: emptyList()
        val version = versions.find { it.id == versionId } ?: throw Exception("Version not found")

        val content = version.sections.joinToString("\n\n") { "${it.title}:\n${it.content}" }
        val prompt = "Analyze this resume against the job description: \nResume: $content\nJob: $jobDescription"

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
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
