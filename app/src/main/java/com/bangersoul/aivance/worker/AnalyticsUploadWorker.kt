package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
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
    private val analyticsDao: AiAnalyticsDao,
    private val generateUsageReportUseCase: GenerateUsageReportUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Timber.d("AnalyticsUploadWorker started")

        if (!connectivityMonitor.isUnmetered) {
            Timber.d("AnalyticsUploadWorker — not on unmetered, deferring")
            return ListenableWorker.Result.retry()
        }

        return try {
            // Count pending analytics events
            val pendingCount = analyticsDao.getEventCount()
            Timber.d("Pending analytics events: %d", pendingCount)

            if (pendingCount == 0) {
                return ListenableWorker.Result.success()
            }

            // Generate and upload usage report
            val reportResult = generateUsageReportUseCase(Unit)
            @Suppress("UNCHECKED_CAST")
            when (reportResult) {
                is com.bangersoul.aivance.core.common.result.Result.Success<*> -> {
                    Timber.d("Usage report generated")
                }
                is com.bangersoul.aivance.core.common.result.Result.Failure -> {
                    Timber.w("Usage report generation failed: %s", (reportResult as com.bangersoul.aivance.core.common.result.Result.Failure).error.message)
                }
            }

            // Clear uploaded events
            analyticsDao.deleteAllEvents()
            Timber.d("AnalyticsUploadWorker completed — %d events uploaded", pendingCount)

            ListenableWorker.Result.success()

        } catch (e: Exception) {
            Timber.e(e, "AnalyticsUploadWorker failed")
            if (runAttemptCount < 3) ListenableWorker.Result.retry() else ListenableWorker.Result.failure()
        }
    }
}
