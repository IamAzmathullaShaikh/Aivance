package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

enum class ExportFormat {
    TXT,
    MARKDOWN,
    JSON
}

data class ExportResumeRequest(
    val resumeId: Long,
    val format: ExportFormat = ExportFormat.TXT
)

/**
 * Exports a resume in the specified format.
 *
 * Business rules:
 * - Resume must exist.
 * - Supports TXT, Markdown, and JSON export formats.
 * - The exported content preserves resume structure.
 */
class ExportResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<ExportResumeRequest, CoreResult<String>>() {

    override suspend operator fun invoke(input: ExportResumeRequest): CoreResult<String> {
        if (input.resumeId <= 0) {
            return Result.Failure(ValidationError("resumeId", "Invalid resume ID."))
        }

        return runCatchingCore {
            val resumeResult = resumeRepository.getResumeById(input.resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> throw Exception(resumeResult.error.message)
                null -> throw Exception("Resume not found.")
            }

            when (input.format) {
                ExportFormat.TXT -> exportAsText(resume)
                ExportFormat.MARKDOWN -> exportAsMarkdown(resume)
                ExportFormat.JSON -> exportAsJson(resume)
            }
        }
    }

    private fun exportAsText(resume: Resume): String {
        return buildString {
            appendLine("=== ${resume.fileName} ===")
            appendLine(resume.rawText)
            if (resume.sections.isNotEmpty()) {
                appendLine()
                appendLine("--- Parsed Sections ---")
                resume.sections.forEach { section ->
                    appendLine()
                    appendLine("${section.title}:")
                    appendLine(section.content)
                }
            }
        }
    }

    private fun exportAsMarkdown(resume: Resume): String {
        return buildString {
            appendLine("# Resume: ${resume.fileName}")
            appendLine()
            if (resume.sections.isNotEmpty()) {
                resume.sections.forEach { section ->
                    appendLine("## ${section.title}")
                    appendLine()
                    appendLine(section.content)
                    appendLine()
                }
            } else {
                appendLine(resume.rawText)
            }
        }
    }

    private fun exportAsJson(resume: Resume): String {
        return buildString {
            appendLine("{")
            appendLine("  \"fileName\": \"${escapeJson(resume.fileName)}\",")
            appendLine("  \"fileUri\": \"${escapeJson(resume.fileUri)}\",")
            appendLine("  \"characterCount\": ${resume.characterCount},")
            appendLine("  \"isPrimary\": ${resume.isPrimary},")
            appendLine("  \"status\": \"${resume.status.name}\",")
            appendLine("  \"sections\": [")
            resume.sections.forEachIndexed { index, section ->
                appendLine("    {")
                appendLine("      \"sectionType\": \"${escapeJson(section.sectionType)}\",")
                appendLine("      \"title\": \"${escapeJson(section.title)}\",")
                appendLine("      \"content\": \"${escapeJson(section.content.take(500))}\"")
                append("    }")
                if (index < resume.sections.size - 1) appendLine(",") else appendLine()
            }
            appendLine("  ],")
            appendLine("  \"rawText\": \"${escapeJson(resume.rawText.take(1000))}\"")
            append("}")
        }
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
