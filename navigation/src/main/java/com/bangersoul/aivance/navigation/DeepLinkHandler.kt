package com.bangersoul.aivance.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Handles incoming deep link intents and converts them into
 * [Destination] objects for navigation.
 *
 * Supported URI patterns:
 *   aivance://jobs/{jobId}
 *   aivance://chat/{conversationId}
 *   aivance://interview/{sessionId}
 *   aivance://roadmap/{roadmapId}
 *   aivance://resume/{resumeId}
 *   aivance://app (generic app open)
 */
object DeepLinkHandler {

    private const val TAG = "DeepLinkHandler"

    /** The most recently parsed deep link destination, if any. */
    @Volatile
    var pendingDestination: Destination? = null
        private set

    /** Same as [pendingDestination] but for warm-start consumption. */
    @Volatile
    var lastDeepLinkDestination: Destination? = null
        private set

    /**
     * Parses an incoming [Intent] and stores the resulting [Destination].
     * Returns `null` if the intent does not contain a known deep link.
     */
    fun handleIntent(intent: Intent): Destination? {
        val uri = intent.data ?: return null
        val destination = parseUri(uri)

        if (destination != null) {
            pendingDestination = destination
            lastDeepLinkDestination = destination
            Log.d(TAG, "Deep link resolved: $uri → $destination")
        } else {
            Log.w(TAG, "Deep link URI not recognised: $uri")
        }

        return destination
    }

    /**
     * Parses a deep-link [Uri] and maps it to a [Destination].
     *
     * Supported schemes:
     * - `aivance://host/path`
     * - `https://aivance.app/host/path`
     */
    fun parseUri(uri: Uri): Destination? {
        val scheme = uri.scheme ?: return null
        val host = uri.host ?: return null
        val pathSegments = uri.pathSegments

        // Only handle aivance:// or https://aivance.app URIs
        val isValidScheme = scheme == "aivance" ||
                (scheme == "https" && host == "aivance.app")

        if (!isValidScheme) return null

        return when (host.lowercase()) {
            "jobs" -> {
                val jobId = pathSegments.firstOrNull() ?: return null
                Destination.JobDetails(jobId = jobId)
            }
            "chat" -> {
                Destination.AiChat
            }
            "interview" -> {
                Destination.Interview
            }
            "roadmap" -> {
                Destination.CareerRoadmap
            }
            "resume" -> {
                Destination.Resume
            }
            "app", "dashboard" -> {
                Destination.Dashboard
            }
            "settings" -> {
                Destination.Settings
            }
            "saved" -> {
                Destination.SavedJobs
            }
            "notifications" -> {
                Destination.Notifications
            }
            else -> null
        }
    }

    /** Clears the pending deep link after it has been consumed. */
    fun consumePending(): Destination? {
        val dest = pendingDestination
        pendingDestination = null
        return dest
    }

    /** Resets all stored deep link state. */
    fun reset() {
        pendingDestination = null
        lastDeepLinkDestination = null
    }
}
