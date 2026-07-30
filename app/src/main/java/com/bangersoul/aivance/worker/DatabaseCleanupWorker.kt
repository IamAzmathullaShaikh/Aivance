package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.database.AivanceDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Database maintenance worker that performs:
 * - WAL checkpoint (flush WAL to main DB)
 * - Integrity check
 * - VACUUM (reclaim storage space)
 *
 * Scheduled weekly during device idle/charging.
 */
@HiltWorker
class DatabaseCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: AivanceDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Timber.d("DatabaseCleanupWorker started")

        return try {
            val db = database.openHelper.writableDatabase

            // 1. WAL checkpoint
            db.execSQL("PRAGMA wal_checkpoint(FULL)")
            Timber.d("WAL checkpoint completed")

            // 2. Integrity check
            val cursor = db.query("PRAGMA integrity_check(1)")
            var integrityOk = true
            while (cursor.moveToNext()) {
                val result = cursor.getString(0)
                if (result != "ok") {
                    Timber.w("Database integrity issue: $result")
                    integrityOk = false
                }
            }
            cursor.close()
            if (integrityOk) {
                Timber.d("Database integrity check passed")
            }

            // 3. VACUUM (reclaim space)
            db.execSQL("VACUUM")
            Timber.d("Database VACUUM completed")

            // 4. Update database statistics for query planner
            db.execSQL("ANALYZE")
            Timber.d("Database statistics updated")

            ListenableWorker.Result.success()

        } catch (e: Exception) {
            Timber.e(e, "DatabaseCleanupWorker failed")
            if (runAttemptCount < 3) ListenableWorker.Result.retry() else ListenableWorker.Result.failure()
        }
    }
}
