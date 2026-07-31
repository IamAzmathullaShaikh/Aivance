package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic worker that captures a snapshot of current career metrics.
 * Scheduled weekly to track long-term progress.
 */
@HiltWorker
class AnalyticsSnapshotWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val analyticsRepository: AnalyticsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Timber.d("AnalyticsSnapshotWorker started")
        return try {
            analyticsRepository.createSnapshot()
            analyticsRepository.refreshRecommendations()
            Timber.d("AnalyticsSnapshotWorker completed")
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.e(e, "AnalyticsSnapshotWorker failed")
            ListenableWorker.Result.retry()
        }
    }
}
