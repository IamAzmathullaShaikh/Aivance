package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.CoverLetterVersion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ExportCoverLetterRequest(
    val coverLetterId: Long,
    val format: ExportLetterFormat = ExportLetterFormat.TXT
)

enum class ExportLetterFormat {
    TXT,
    MARKDOWN,
    PLAIN_TEXT
}

/**
 * Exports a cover letter in the specified format.
 */
class ExportCoverLetterUseCase @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository
) : UseCase<ExportCoverLetterRequest, CoreResult<String>>() {

    override suspend operator fun invoke(input: ExportCoverLetterRequest): CoreResult<String> {
        if (input.coverLetterId <= 0) {
            return Result.Failure(ValidationError("coverLetterId", "Invalid cover letter ID."))
        }

        return runCatchingCore {
            val letterResult = coverLetterRepository.getCoverLetterById(input.coverLetterId).firstOrNull()
            val letter = when (letterResult) {
                is Result.Success -> letterResult.data
                is Result.Failure -> throw Exception(letterResult.error.message)
                null -> throw Exception("Cover letter not found.")
            }

            val version = letter.versions.firstOrNull() ?: throw Exception("No versions found")

            when (input.format) {
                ExportLetterFormat.TXT -> exportAsText(letter, version)
                ExportLetterFormat.MARKDOWN -> exportAsMarkdown(letter, version)
                ExportLetterFormat.PLAIN_TEXT -> version.sections.joinToString("\n\n") { it.content }
            }
        }
    }

    private fun exportAsText(letter: CoverLetter, version: CoverLetterVersion): String {
        return buildString {
            appendLine("=== Cover Letter ===")
            appendLine("Company: ${letter.company}")
            appendLine("Position: ${letter.role}")
            appendLine("Style: ${version.writingStyle}")
            appendLine("Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(letter.dateCreated))}")
            appendLine()
            version.sections.forEach { appendLine(it.content); appendLine() }
        }
    }

    private fun exportAsMarkdown(letter: CoverLetter, version: CoverLetterVersion): String {
        return buildString {
            appendLine("# Cover Letter")
            appendLine()
            appendLine("**Company:** ${letter.company}")
            appendLine("**Position:** ${letter.role}")
            appendLine("**Style:** ${version.writingStyle}")
            appendLine()
            appendLine("---")
            appendLine()
            version.sections.forEach { appendLine(it.content); appendLine() }
        }
    }
}
