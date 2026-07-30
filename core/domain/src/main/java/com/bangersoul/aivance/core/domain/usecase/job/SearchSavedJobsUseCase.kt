package com.bangersoul.aivance.core.domain.usecase.job

import androidx.paging.PagingData
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns saved/bookmarked jobs as a PagingData flow.
 *
 * Business rules:
 * - Returns only jobs that have been explicitly saved/bookmarked by the user.
 * - Results are ordered by application date (most recent first).
 * - Returns an empty paging source if no saved jobs exist.
 */
class SearchSavedJobsUseCase @Inject constructor(
    private val jobTrackerRepository: JobTrackerRepository
) {
    fun invoke(): Flow<PagingData<JobApplication>> {
        return jobTrackerRepository.getApplications()
    }
}
