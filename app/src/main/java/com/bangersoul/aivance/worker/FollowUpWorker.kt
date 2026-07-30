package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Periodic worker that checks for stale job applications (3+ days with no follow-up)
 * and sends follow-up reminder notifications.
 */
@HiltWorker
class FollowUpWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val trackerDao: TrackerDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli()

        val apps = trackerDao.getApplications().firstOrNull() ?: emptyList()
        for (app in apps) {
            val isStale = app.application.dateApplied <= threeDaysAgo &&
                app.application.status == "APPLIED"
            if (isStale) {
                notificationHelper.showFollowUpNotification(
                    id = app.application.id.toInt(),
                    title = "Follow-up Reminder",
                    message = "It's been 3 days since you applied to ${app.application.company}. Consider following up!"
                )
            }
        }

        return ListenableWorker.Result.success()
    }
}
