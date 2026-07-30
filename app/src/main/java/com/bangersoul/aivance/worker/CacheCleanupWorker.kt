package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.database.dao.JobDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Periodic worker that cleans up stale data:
 * - Evicts expired job listings older than 30 days
 * - Removes analytics events older than 90 days
 * - Cleans up old AI conversation history
 * - Performs database WAL checkpoint
 *
 * Scheduled daily, runs with no network requirement.
 */
@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val jobDao: JobDao,
    private val analyticsDao: AiAnalyticsDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Timber.d("CacheCleanupWorker started")

        return try {
            val now = Instant.now()

            // 1. Delete stale job listings (> 30 days old)
            val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS).toEpochMilli()
            val deletedJobs = jobDao.deleteJobsOlderThan(thirtyDaysAgo)
            Timber.d("Deleted %d stale job listings", deletedJobs)

            // 2. Delete old analytics events (> 90 days old)
            val ninetyDaysAgo = now.minus(90, ChronoUnit.DAYS).toEpochMilli()
            val deletedEvents = analyticsDao.deleteEventsBefore(ninetyDaysAgo)
            Timber.d("Deleted %d old analytics events", deletedEvents)

            // 3. Clean up old AI conversations (> 60 days old)
            val sixtyDaysAgo = now.minus(60, ChronoUnit.DAYS).toEpochMilli()
            val deletedConversations = analyticsDao.deleteOldConversations(sixtyDaysAgo)
            Timber.d("Deleted %d old AI conversations", deletedConversations)

            Timber.d("CacheCleanupWorker completed")
            ListenableWorker.Result.success()

        } catch (e: Exception) {
            Timber.e(e, "CacheCleanupWorker failed")
            if (runAttemptCount < 3) ListenableWorker.Result.retry() else ListenableWorker.Result.failure()
        }
    }
}
