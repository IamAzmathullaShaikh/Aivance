package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.room.Room
import androidx.work.CoroutineWorker
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

    override suspend fun doWork(): Result {
        Timber.d("DatabaseCleanupWorker started")

        return try {
            val db = database.openHelper.writableDatabase
            val supportDb = database.getOpenHelper().writableDatabase

            // 1. WAL checkpoint
            supportDb.execSQL("PRAGMA wal_checkpoint(FULL)")
            Timber.d("WAL checkpoint completed")

            // 2. Integrity check
            val cursor = supportDb.query("PRAGMA integrity_check(1)")
            var integrityOk = true
            cursor?.use {
                while (it.moveToNext()) {
                    val result = it.getString(0)
                    if (result != "ok") {
                        Timber.w("Database integrity issue: %s", result)
                        integrityOk = false
                    }
                }
            }
            if (integrityOk) {
                Timber.d("Database integrity check passed")
            }

            // 3. VACUUM (reclaim space)
            supportDb.execSQL("VACUUM")
            Timber.d("Database VACUUM completed")

            // 4. Update database statistics for query planner
            supportDb.execSQL("ANALYZE")
            Timber.d("Database statistics updated")

            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "DatabaseCleanupWorker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
