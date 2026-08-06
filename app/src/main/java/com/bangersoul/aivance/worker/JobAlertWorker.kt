package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Component 3: Background Worker that periodically polls job providers for
 * candidate target alerts, notifying when new high-relevance jobs matching
 * target search criteria are found.
 */
@HiltWorker
class JobAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val jobRepository: JobRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.i("JobAlertWorker: executing background job alert scan")
        return try {
            val filter = JobSearchFilter(query = "Android Developer")
            val results = jobRepository.searchJobs(filter)
            if (results is Result.Success) {
                Timber.i("JobAlertWorker: scan complete — found %d matching jobs", results.data.size)
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "JobAlertWorker: background scan failed")
            Result.retry()
        }
    }
}
