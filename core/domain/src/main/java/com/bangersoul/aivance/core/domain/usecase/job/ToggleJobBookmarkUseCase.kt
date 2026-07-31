package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Toggles a job's bookmark status.
 */
class ToggleJobBookmarkUseCase @Inject constructor(
    private val jobRepository: JobRepository
) : UseCase<String, CoreResult<Boolean>>() {

    override suspend operator fun invoke(input: String): CoreResult<Boolean> {
        return jobRepository.toggleBookmark(input)
    }
}
