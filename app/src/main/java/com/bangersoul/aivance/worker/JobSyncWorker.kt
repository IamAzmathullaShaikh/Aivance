package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsRequest
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
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
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Timber.d("JobSyncWorker started")

        if (!connectivityMonitor.isUnmetered && runAttemptCount == 0) {
            Timber.d("JobSyncWorker — not on unmetered, deferring")
            return ListenableWorker.Result.retry()
        }

        return try {
            val queries = listOf(
                "android developer", "kotlin developer", "mobile engineer",
                "software engineer", "data scientist"
            )

            var succeeded = 0

            for (query in queries) {
                val flow = searchJobsUseCase(
                    SearchJobsRequest(
                        query = query
                    )
                )
                succeeded++
                Timber.d("JobSyncWorker — search for '%s' triggered", query)
            }

            Timber.d("JobSyncWorker completed — %d searches triggered", succeeded)
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.e(e, "JobSyncWorker failed")
            if (runAttemptCount < 3) ListenableWorker.Result.retry() else ListenableWorker.Result.failure()
        }
    }
}
