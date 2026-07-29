package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.database.dao.ApplicationDao
import com.bangersoul.aivance.core.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class FollowUpWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val applicationDao: ApplicationDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli()
        // Using "APPLIED" as it matches ApplicationStatus.APPLIED.name
        val staleApplications = applicationDao.getApplicationsByStatusAndStale("APPLIED", threeDaysAgo)

        if (staleApplications.isNotEmpty()) {
            staleApplications.forEach { app ->
                notificationHelper.showFollowUpNotification(
                    id = app.id.toInt(),
                    title = "Follow-up Reminder",
                    message = "It's been 3 days since you applied to ${app.company} for the ${app.role} role. Consider following up!"
                )
            }
        }

        return Result.success()
    }
}
