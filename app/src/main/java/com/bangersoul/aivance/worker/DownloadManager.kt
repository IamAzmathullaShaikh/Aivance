package com.bangersoul.aivance.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bangersoul.aivance.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages file downloads with foreground notification support.
 *
 * Used for downloading:
 * - Resume export PDFs
 * - Shared document previews
 * - Cover letter exports
 */
@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "downloads"
        private const val CHANNEL_NAME = "File Downloads"
        private const val NOTIFICATION_ID_BASE = 1000
        private const val BUFFER_SIZE = 8192
    }

    init {
        // minSdk is 26, so NotificationChannel is always available.
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Download progress notifications" }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    /**
     * Downloads a file from [url] and saves it to the app's downloads directory.
     * Shows progress notifications during download.
     *
     * Every notify() call below is guarded by an explicit POST_NOTIFICATIONS
     * runtime check, so the lint MissingPermission warning is a false positive.
     *
     * @return The downloaded [File] on success, null on failure.
     */
    @SuppressLint("MissingPermission")
    suspend fun downloadFile(
        url: String,
        fileName: String? = null,
        title: String = "Downloading file"
    ): File? = withContext(Dispatchers.IO) {
        val notifId = NOTIFICATION_ID_BASE + UUID.randomUUID().hashCode().ushr(1) % 1000
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.connect()

            val contentLength = connection.contentLengthLong
            val inputStream = connection.inputStream
            val safeFileName = fileName ?: url.substringAfterLast("/").ifBlank { "download_${System.currentTimeMillis()}.pdf" }
            val outputFile = File(context.getExternalFilesDir(null), safeFileName)

            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    // Update progress notification every ~10%
                    if (contentLength > 0) {
                        val progress = (totalBytesRead * 100 / contentLength).toInt()
                        if (progress % 10 == 0) {
                            val notification = createProgressNotification(
                                title = title,
                                progress = progress,
                                pendingIntent = pendingIntent
                            )
                            // Safely notify if POST_NOTIFICATIONS permission is granted
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                notificationManager.notify(notifId, notification)
                            }
                        }
                    }
                }
            }

            inputStream.close()
            connection.disconnect()

            // Success notification
            val successNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText(safeFileName)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            // Notify success safely
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notifId, successNotification)
            }

            Timber.d("Downloaded: %s (%d bytes)", safeFileName, outputFile.length())
            outputFile

        } catch (e: Exception) {
            Timber.e(e, "Download failed: %s", url)
            val errorNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Download Failed")
                .setContentText(e.message ?: "Unknown error")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            // Notify error safely
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notifId, errorNotification)
            }
            null
        }
    }

    private fun createProgressNotification(
        title: String,
        progress: Int,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText("$progress%")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
