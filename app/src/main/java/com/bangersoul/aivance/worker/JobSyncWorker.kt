package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsRequest
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic worker that fetches fresh job listings from remote providers
 * and caches them in the local Room database.
 */
@HiltWorker
class JobSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val searchJobsUseCase: SearchJobsUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Timber.d("JobSyncWorker started")

        if (!connectivityMonitor.isUnmetered && runAttemptCount == 0) {
            Timber.d("JobSyncWorker — not on unmetered, deferring")
            return ListenableWorker.Result.retry()
        }

        return try {
            val queries = listOf("Android Developer", "Mobile Engineer", "Kotlin")

            for (q in queries) {
                searchJobsUseCase(SearchJobsRequest(filter = JobSearchFilter(query = q)))
            }

            Timber.d("JobSyncWorker completed")
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.e(e, "JobSyncWorker failed")
            if (runAttemptCount < 3) ListenableWorker.Result.retry() else ListenableWorker.Result.failure()
        }
    }
}
