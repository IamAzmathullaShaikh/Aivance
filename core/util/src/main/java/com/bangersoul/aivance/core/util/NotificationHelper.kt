package com.bangersoul.aivance.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced notification helper that manages multiple notification channels
 * with deep-link support and category-specific methods.
 *
 * Note: Uses the package manager to resolve the launch intent rather than
 * referencing a specific Activity class, avoiding circular dependencies
 * between core modules and the app module.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_FOLLOW_UPS = "follow_up_notifications"
        const val CHANNEL_FOLLOW_UPS_NAME = "Job Follow-ups"
        const val CHANNEL_FOLLOW_UPS_DESC = "Reminders to follow up on job applications"

        const val CHANNEL_INTERVIEWS = "interview_reminders"
        const val CHANNEL_INTERVIEWS_NAME = "Interview Reminders"
        const val CHANNEL_INTERVIEWS_DESC = "Reminders for upcoming mock interviews"

        const val CHANNEL_APPLICATIONS = "application_updates"
        const val CHANNEL_APPLICATIONS_NAME = "Application Updates"
        const val CHANNEL_APPLICATIONS_DESC = "Updates on your job applications"

        const val CHANNEL_SYNC = "sync_notifications"
        const val CHANNEL_SYNC_NAME = "Sync & System"
        const val CHANNEL_SYNC_DESC = "Background sync completion and system notifications"
    }

    init {
        createAllNotificationChannels()
    }

    private fun createAllNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_FOLLOW_UPS, CHANNEL_FOLLOW_UPS_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = CHANNEL_FOLLOW_UPS_DESC
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_INTERVIEWS, CHANNEL_INTERVIEWS_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = CHANNEL_INTERVIEWS_DESC
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_APPLICATIONS, CHANNEL_APPLICATIONS_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = CHANNEL_APPLICATIONS_DESC
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_SYNC, CHANNEL_SYNC_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                    description = CHANNEL_SYNC_DESC
                }
            )
        }
    }

    fun showFollowUpNotification(id: Int, title: String, message: String) {
        showNotification(id, CHANNEL_FOLLOW_UPS, title, message, deepLinkUri = "aivance://saved")
    }

    fun showInterviewReminder(id: Int, title: String, message: String) {
        showNotification(id, CHANNEL_INTERVIEWS, title, message, deepLinkUri = "aivance://interview")
    }

    fun showApplicationUpdate(id: Int, title: String, message: String) {
        showNotification(id, CHANNEL_APPLICATIONS, title, message, deepLinkUri = "aivance://saved")
    }

    fun showSyncNotification(id: Int, title: String, message: String) {
        showNotification(id, CHANNEL_SYNC, title, message, deepLinkUri = "aivance://app")
    }

    fun showJobAlert(id: Int, title: String, message: String) {
        showNotification(id, CHANNEL_APPLICATIONS, title, message, deepLinkUri = "aivance://jobs")
    }

    private fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        message: String,
        deepLinkUri: String? = null,
        autoCancel: Boolean = true
    ) {
        val deepLinkIntent = if (deepLinkUri != null) {
            Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(deepLinkUri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            // Use the launcher intent instead of referencing a specific Activity class
            // to avoid circular module dependencies
            val launchIntent = context.packageManager.getLaunchIntentForPackage(
                context.packageName
            )?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            } ?: Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            launchIntent
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                when (channelId) {
                    CHANNEL_INTERVIEWS -> NotificationCompat.PRIORITY_HIGH
                    CHANNEL_SYNC -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(autoCancel)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (e: SecurityException) {
            // Handle missing notification permission on Android 13+
        }
    }
}
