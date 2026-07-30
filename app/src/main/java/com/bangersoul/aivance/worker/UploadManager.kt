package com.bangersoul.aivance.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import java.io.InputStream
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bangersoul.aivance.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Upload result.
 */
sealed interface UploadResult {
    data class Success(val serverUrl: String) : UploadResult
    data class Failure(val message: String) : UploadResult
}

/**
 * Manages file uploads with throttled progress notification support.
 *
 * Used for:
 * - Resume PDF upload to providers
 * - Analytics data export upload
 * - Cover letter export upload
 */
@Singleton
class UploadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val CHANNEL_ID = "uploads"
        private const val CHANNEL_NAME = "File Uploads"
        private const val NOTIFICATION_ID_BASE = 2000
        private const val BUFFER_SIZE = 8192
        private const val MIN_PROGRESS_INTERVAL_MS = 500L
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Upload progress notifications" }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Uploads a file from the given [uri] to [serverUrl] with throttled progress notifications.
     */
    suspend fun uploadFile(
        serverUrl: String,
        uri: Uri,
        contentType: String = "application/pdf",
        title: String = "Uploading file"
    ): UploadResult = withContext(Dispatchers.IO) {
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
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(uri)
                ?: return@withContext UploadResult.Failure("Cannot open file")

            val fileBytes = inputStream.readBytes()
            inputStream.close()

            val mediaType = contentType.toMediaType()
            val totalBytes = fileBytes.size
            var lastNotificationTime = 0L

            val requestBody = object : RequestBody() {
                override fun contentType() = mediaType
                override fun contentLength() = totalBytes.toLong()

                override fun writeTo(sink: okio.BufferedSink) {
                    val buffer = ByteArray(BUFFER_SIZE)
                    var offset = 0
                    var lastReportedProgress = -1

                    while (offset < totalBytes) {
                        val bytesToWrite = minOf(BUFFER_SIZE, totalBytes - offset)
                        sink.write(fileBytes, offset, bytesToWrite)
                        offset += bytesToWrite

                        val currentTime = System.currentTimeMillis()
                        val progress = (offset * 100 / totalBytes)

                        // Throttle: only update every 500ms or every 5% progress, whichever is sooner
                        if (progress != lastReportedProgress &&
                            (currentTime - lastNotificationTime >= MIN_PROGRESS_INTERVAL_MS || progress % 5 == 0)) {
                            lastReportedProgress = progress
                            lastNotificationTime = currentTime

                            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.stat_sys_upload)
                                .setContentTitle(title)
                                .setContentText("$progress%")
                                .setProgress(100, progress, false)
                                .setOngoing(true)
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .build()
                            notificationManager.notify(notifId, notification)
                        }
                    }
                }
            }

            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "upload.pdf", requestBody)
                .build()

            val request = Request.Builder()
                .url(serverUrl)
                .post(multipart)
                .build()

            val response = okHttpClient.newCall(request).execute()

            return@withContext if (response.isSuccessful) {
                val successNotif = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setContentTitle("Upload Complete")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()
                notificationManager.notify(notifId, successNotif)
                UploadResult.Success(response.body?.string() ?: serverUrl)
            } else {
                val errorNotif = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("Upload Failed")
                    .setContentText("Server error: ${response.code}")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()
                notificationManager.notify(notifId, errorNotif)
                UploadResult.Failure("HTTP ${response.code}: ${response.message}")
            }

        } catch (e: Exception) {
            Timber.e(e, "Upload failed")
            val errorNotif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Upload Failed")
                .setContentText(e.message ?: "Unknown error")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify(notifId, errorNotif)
            UploadResult.Failure(e.message ?: "Upload failed")
        }
    }
}

