package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Removes a previously saved/bookmarked job application.
 *
 * Business rules:
 * - Application must exist before removal.
 * - Once removed, the application is permanently deleted (no undo).
 */
class RemoveSavedJobUseCase @Inject constructor(
    private val jobTrackerRepository: JobTrackerRepository
) : UseCase<Long, CoreResult<Unit>>() {

    override suspend operator fun invoke(applicationId: Long): CoreResult<Unit> {
        if (applicationId <= 0) {
            return Result.Failure(ValidationError("applicationId", "Invalid application ID."))
        }

        return runCatchingCore {
            jobTrackerRepository.deleteApplication(applicationId)
        }
    }
}
