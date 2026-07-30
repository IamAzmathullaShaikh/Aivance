package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.database.dao.AnalyticsDao
import com.bangersoul.aivance.core.domain.usecase.analytics.GenerateUsageReportUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * One-time worker that uploads batched analytics events to the remote server.
 *
 * Triggered after every 50 logged events, or hourly via periodic schedule.
 * Runs only on unmetered (Wi-Fi) connections to conserve mobile data.
 */
@HiltWorker
class AnalyticsUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val analyticsDao: AnalyticsDao,
    private val generateUsageReportUseCase: GenerateUsageReportUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("AnalyticsUploadWorker started")

        if (!connectivityMonitor.isUnmetered) {
            Timber.d("AnalyticsUploadWorker — not on unmetered, deferring")
            return Result.retry()
        }

        return try {
            // Collect pending events from the local analytics DAO
            val pendingCount = analyticsDao.getPendingEventCount()
            Timber.d("Pending analytics events: %d", pendingCount)

            if (pendingCount == 0L) {
                return Result.success()
            }

            // Generate and upload usage report
            val reportResult = generateUsageReportUseCase()
            when (reportResult) {
                is Result.Success -> {
                    Timber.d("Usage report generated: %s", reportResult.data)
                }
                is Result.Failure -> {
                    Timber.w("Usage report generation failed: %s", reportResult.error.message)
                }
            }

            // Mark events as synced
            analyticsDao.markEventsAsSynced()
            Timber.d("AnalyticsUploadWorker completed — %d events uploaded", pendingCount)

            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "AnalyticsUploadWorker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
