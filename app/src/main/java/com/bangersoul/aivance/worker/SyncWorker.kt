package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic worker that drains the [SyncManager]'s pending operations queue.
 *
 * Scheduled by [com.bangersoul.aivance.AivanceApp] every 15 minutes
 * when the device is connected and idle.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker started — %d pending operations", syncManager.pendingOperationCount())

        if (syncManager.pendingOperationCount() == 0) {
            return Result.success()
        }

        return try {
            syncManager.drainQueue()

            when (syncManager.syncState.value) {
                SyncState.SUCCESS -> {
                    Timber.d("SyncWorker completed successfully")
                    Result.success()
                }
                SyncState.PARTIAL_FAILURE -> {
                    Timber.w("SyncWorker completed with partial failures")
                    syncManager.scheduleRetryWorker()
                    Result.success()
                }
                SyncState.FAILURE -> {
                    Timber.e("SyncWorker failed")
                    syncManager.scheduleRetryWorker()
                    Result.retry()
                }
                SyncState.OFFLINE -> {
                    Timber.d("SyncWorker — device offline, rescheduling")
                    Result.retry()
                }
                else -> {
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker crashed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
