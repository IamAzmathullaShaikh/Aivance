package com.bangersoul.aivance.core.domain.usecase.resume

import android.net.Uri
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * High-level use case to import a resume file.
 */
class ImportResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<Uri, CoreResult<Long>>() {

    override suspend operator fun invoke(input: Uri): CoreResult<Long> {
        return resumeRepository.importResume(input)
    }
}
