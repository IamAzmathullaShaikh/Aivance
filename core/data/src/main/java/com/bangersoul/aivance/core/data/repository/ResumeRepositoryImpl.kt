package com.bangersoul.aivance.core.data.repository

import android.content.Context
import android.net.Uri
import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.JobDescription
import com.bangersoul.aivance.core.common.model.OptimizationTip
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.data.resume.ResumeParser
import com.bangersoul.aivance.core.data.source.ResumeLocalDataSource
import com.bangersoul.aivance.core.database.dao.AtsDao
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
    private val resumeParser: ResumeParser,
    private val atsDao: AtsDao
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
        val fileName = resolveDisplayName(uri) ?: uri.lastPathSegment ?: "Imported Resume"
        var extension = fileName.substringAfterLast(".", "").lowercase()
        if (extension.isBlank()) {
            // Picker content:// URIs rarely expose the filename via lastPathSegment
            // (they return an opaque numeric document id), so fall back to the MIME
            // type when the resolved name has no extension.
            extension = mimeTypeToExtension(context.contentResolver.getType(uri))
        }

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

    /**
     * Resolves the real file name for a picker URI. content:// URIs hide the
     * filename in ContentResolver metadata (OpenableColumns.DISPLAY_NAME);
     * lastPathSegment alone is typically an opaque document id, which is why
     * valid PDFs picked from the system picker were rejected as unsupported.
     */
    private fun resolveDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun mimeTypeToExtension(mime: String?): String = when (mime?.lowercase()) {
        "application/pdf" -> "pdf"
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
        else -> ""
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

    override suspend fun analyzeResume(resumeId: Long, versionId: Long, jobDescription: String): CoreResult<AtsReport> = runCatchingCore {
        val versions = localDataSource.getVersionsForResume(resumeId).firstOrNull() ?: emptyList()
        val version = versions.find { it.id == versionId } ?: throw Exception("Version not found")

        val content = version.sections.joinToString("\n\n") { "${it.title}:\n${it.content}" }
        val prompt = "Analyze this resume against the job description. " +
            "Return: overall match score 0-100, matched keywords, missing keywords, and 3 optimization tips.\n" +
            "Resume:\n$content\n\nJob Description:\n$jobDescription"

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val aiResponse = provider.generateText(prompt).getOrNull() ?: throw Exception("AI analysis failed")

        // Persist the job description first: ats_reports.jobDescriptionId is an
        // enforced FK (Room enables foreign_keys), so a bare 0 would violate it.
        val jobDescriptionId = atsDao.insertJobDescription(
            JobDescription(rawText = jobDescription).toEntity()
        )

        // Persist to ats_reports and return the canonical AtsReport.
        val report = AtsReport(
            resumeVersionId = versionId,
            jobDescriptionId = jobDescriptionId,
            overallScore = 80, // TODO: parse aiResponse for a real score
            matchPercentage = 80,
            optimizationTips = listOf(OptimizationTip("AI", aiResponse, "MEDIUM"))
        )
        val reportId = atsDao.insertReport(report.toEntity())
        report.copy(id = reportId)
    }
}

