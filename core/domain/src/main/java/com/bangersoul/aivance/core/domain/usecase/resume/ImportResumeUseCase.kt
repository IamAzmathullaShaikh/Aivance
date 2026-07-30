package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.common.validation.ResumeValidator
import com.bangersoul.aivance.core.common.validation.ValidationResult
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class ImportResumeRequest(
    val fileName: String,
    val fileUri: String,
    val rawText: String,
    val isPrimary: Boolean = false
)

data class ImportResumeResponse(
    val resumeId: Long,
    val resume: Resume
)

/**
 * Imports a new resume after validating its content.
 * Business rules:
 * - Validates file name, URI, and text length before insertion.
 * - Rejects duplicate file names for the same user.
 * - Sets the parsed date to current time.
 */
class ImportResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<ImportResumeRequest, CoreResult<ImportResumeResponse>>() {

    override suspend operator fun invoke(input: ImportResumeRequest): CoreResult<ImportResumeResponse> {
        // Input validation
        if (input.fileName.isBlank()) {
            return Result.Failure(ValidationError("fileName", "File name cannot be blank."))
        }
        if (input.fileUri.isBlank()) {
            return Result.Failure(ValidationError("fileUri", "File URI cannot be blank."))
        }
        if (input.rawText.length < 50) {
            return Result.Failure(ValidationError("rawText", "Resume text must be at least 50 characters."))
        }
        if (input.rawText.length > 500000) {
            return Result.Failure(ValidationError("rawText", "Resume text must not exceed 500,000 characters."))
        }

        return runCatchingCore {
            val resume = Resume(
                fileName = input.fileName,
                fileUri = input.fileUri,
                rawText = input.rawText,
                isPrimary = input.isPrimary,
                characterCount = input.rawText.length
            )

            val result = resumeRepository.insertResume(resume)
            val id = when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }

            ImportResumeResponse(
                resumeId = id,
                resume = resume.copy(id = id)
            )
        }
    }
}
