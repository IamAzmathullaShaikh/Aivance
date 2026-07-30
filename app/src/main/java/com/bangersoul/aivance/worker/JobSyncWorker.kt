package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic worker that fetches fresh job listings from remote providers
 * and caches them in the local Room database.
 *
 * Scheduled every 2 hours on unmetered (Wi-Fi) connections.
 */
@HiltWorker
class JobSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val searchJobsUseCase: SearchJobsUseCase,
    private val jobDao: JobDao,
    private val companyDao: CompanyDao,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("JobSyncWorker started")

        if (!connectivityMonitor.isUnmetered && runAttemptCount == 0) {
            Timber.d("JobSyncWorker — not on unmetered, deferring")
            return Result.retry()
        }

        return try {
            // Search for jobs across multiple categories
            val queries = listOf(
                "android developer", "kotlin developer", "mobile engineer",
                "software engineer", "data scientist"
            )

            var totalJobs = 0

            for (query in queries) {
                val result = searchJobsUseCase(
                    SearchJobsUseCase.SearchJobsRequest(
                        query = query,
                        remoteOnly = false,
                        maxResults = 20
                    )
                )

                when (result) {
                    is Result.Success -> {
                        // PagingData cannot be directly iterated, but the use case
                        // internally caches results. We just trigger the search.
                        totalJobs++
                        Timber.d("JobSyncWorker — triggered search for '%s'", query)
                    }
                    is Result.Failure -> {
                        Timber.w("JobSyncWorker — search for '%s' failed: %s",
                            query, result.error.message)
                    }
                }
            }

            Timber.d("JobSyncWorker completed — %d searches triggered", totalJobs)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "JobSyncWorker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
