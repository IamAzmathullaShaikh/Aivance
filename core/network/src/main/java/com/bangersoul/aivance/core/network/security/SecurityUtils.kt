package com.bangersoul.aivance.core.network.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.NetworkSecurityPolicy
import java.io.File
import java.net.URL

/**
 * Security utilities for input validation, output encoding, and
 * environment integrity checks.
 */
object SecurityUtils {

    // ── Input Validation ──────────────────────────────────────────

    /**
     * Sanitize user-provided input to prevent injection attacks.
     * Removes HTML/JS and normalises whitespace.
     */
    fun sanitizeInput(input: String?): String {
        if (input == null) return ""
        return input
            .replace(Regex("<[^>]*>"), "")   // Strip HTML tags
            .replace(Regex("[&<>\"']"), "")  // Strip HTML special chars
            .trim()
    }

    /**
     * Validate an external URL to prevent open-redirect and SSRF attacks.
     */
    fun validateExternalUrl(url: String): Boolean {
        return try {
            val parsed = URL(url)
            val scheme = parsed.protocol
            val host = parsed.host?.lowercase() ?: return false

            // Only allow HTTPS
            if (scheme != "https") return false

            // Block IP-based URLs (SSRF prevention)
            if (host.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"))) {
                return false
            }

            // Block localhost / private IPs
            val blockedHosts = setOf(
                "localhost", "127.0.0.1", "0.0.0.0",
                "10.", "172.16.", "172.17.", "172.18.", "172.19.",
                "172.20.", "172.21.", "172.22.", "172.23.", "172.24.",
                "172.25.", "172.26.", "172.27.", "172.28.", "172.29.",
                "172.30.", "172.31.", "192.168."
            )
            if (blockedHosts.any { host.startsWith(it) }) return false

            // Reject URLs with user info (credential smuggling)
            if (parsed.userInfo != null) return false

            // Limit path depth to prevent path traversal
            val path = parsed.path
            if (path.contains("..")) return false

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate a deep-link URI for the aivance:// scheme.
     */
    fun validateDeepLink(uri: String): Boolean {
        return try {
            val parsed = URL(uri)
            val scheme = parsed.protocol
            val host = parsed.host?.lowercase() ?: return false

            if (scheme != "aivance") return false

            val allowedHosts = setOf(
                "jobs", "chat", "interview", "roadmap",
                "resume", "app", "settings", "profile"
            )
            host in allowedHosts
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitise a filename for safe file operations.
     */
    fun sanitiseFilename(name: String): String {
        return name
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), "_")
            .take(128)
    }

    // ── Environment Integrity ─────────────────────────────────────

    /**
     * Check if the app is running on a rooted device (detection abstraction).
     */
    fun isDeviceRooted(context: Context): Boolean {
        // Check for known root binaries
        val rootPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        if (rootPaths.any { File(it).exists() }) return true

        // Check for root management apps
        val rootPackages = setOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.topjohnwu.magisk",
            "com.thirdparty.superuser",
            "com.kingo.root",
            "com.kingroot.master",
            "com.superuser"
        )
        val pm = context.packageManager
        for (pkg in rootPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: PackageManager.NameNotFoundException) {
                // Package not installed
            }
        }

        return false
    }

    /**
     * Check if the device is running a debuggable or emulated environment.
     * Useful for disabling sensitive operations in development builds.
     */
    fun isDebuggableEnvironment(context: Context): Boolean {
        if (Build.FINGERPRINT.contains("generic")) return true
        if (Build.PRODUCT.contains("emulator")) return true
        if (Build.MODEL.contains("Emulator")) return true
        if (Build.MANUFACTURER.contains("Genymotion")) return true
        if (Build.HARDWARE.contains("goldfish")) return true
        if (Build.HARDWARE.contains("ranchu")) return true

        // Check for known emulator files
        val emulatorFiles = listOf(
            "/proc/tty/driver/serial",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace"
        )
        if (emulatorFiles.any { File(it).exists() }) return true

        // Check debug flag
        return (context.applicationInfo.flags and
            android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // ── Network Security ──────────────────────────────────────────

    /**
     * Check if cleartext traffic is permitted (should only be in debug builds).
     */
    fun isCleartextTrafficPermitted(context: Context): Boolean {
        return NetworkSecurityPolicy.getInstance()
            .isCleartextTrafficPermitted()
    }

    /**
     * Validate that a file path is within the app's private storage.
     */
    fun isPathWithinAppStorage(context: Context, path: String): Boolean {
        val canonical = File(path).canonicalPath
        val appDir = context.filesDir.canonicalPath
        val cacheDir = context.cacheDir.canonicalPath
        return canonical.startsWith(appDir) || canonical.startsWith(cacheDir)
    }
}
