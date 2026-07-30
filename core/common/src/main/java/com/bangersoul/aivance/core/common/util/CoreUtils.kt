package com.bangersoul.aivance.core.common.util

import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.serialization.json.Json

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<out T>(val data: T) : Resource<T>
    data class Error<out T>(val exception: Throwable, val data: T? = null) : Resource<T>

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

object DateUtils {
    private const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val DATE_DISPLAY_FORMAT = "MMM dd, yyyy"
    private const val TIME_DISPLAY_FORMAT = "hh:mm a"

    fun formatIso8601(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat(ISO_8601_FORMAT, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }

    fun parseIso8601(isoString: String): Long? {
        return try {
            val sdf = SimpleDateFormat(ISO_8601_FORMAT, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(isoString)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun formatDateDisplay(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_DISPLAY_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTimeDisplay(timestamp: Long): String {
        val sdf = SimpleDateFormat(TIME_DISPLAY_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getElapsedTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diffMs = now - timestamp
        if (diffMs < 0) return "Just now"
        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days}d ago"
            else -> formatDateDisplay(timestamp)
        }
    }
}

object IdGenerator {
    fun generateUuid(): String = UUID.randomUUID().toString()

    fun generatePrefixedId(prefix: String): String {
        val cleanedPrefix = prefix.lowercase().replace(Regex("[^a-z0-9]"), "")
        return "${cleanedPrefix}_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    }
}

object HashUtils {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

object FormatUtils {
    fun formatCurrency(amount: Double, locale: Locale = Locale.US): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(amount)
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceAtMost(units.size - 1)
        return "%.1f %s".format(bytes / Math.pow(1024.0, index.toDouble()), units[index])
    }

    fun truncateText(text: String, maxLength: Int, ellipsis: String = "..."): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - ellipsis.length) + ellipsis
    }
}

object JsonUtils {
    val defaultJson = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun isValidJson(jsonString: String): Boolean {
        return try {
            defaultJson.parseToJsonElement(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }
}

object FileUtils {
    fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex != -1 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex + 1).lowercase()
        } else {
            ""
        }
    }

    fun getMimeType(fileName: String): String {
        return when (getFileExtension(fileName)) {
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "doc", "docx" -> "application/msword"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "application/octet-stream"
        }
    }

    fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}

object UriUtils {
    fun isValidUri(uriString: String): Boolean {
        if (uriString.isBlank()) return false
        return try {
            val uri = URI(uriString)
            uri.scheme != null
        } catch (e: Exception) {
            false
        }
    }

    fun extractQueryParam(uriString: String, paramName: String): String? {
        return try {
            val uri = URI(uriString)
            val query = uri.query ?: return null
            query.split("&")
                .map { it.split("=") }
                .firstOrNull { it.size == 2 && it[0] == paramName }
                ?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
