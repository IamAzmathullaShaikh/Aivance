package com.bangersoul.aivance.core.domain.config

/**
 * Manages application versioning following semantic versioning.
 *
 * versionName format: MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
 * versionCode format: MAJOR * 1000000 + MINOR * 10000 + PATCH * 100 + BUILD
 */
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val build: Int = 0,
    val prerelease: String? = null
) {
    val versionName: String
        get() {
            val base = "$major.$minor.$patch"
            return if (prerelease != null) "$base-$prerelease" else base
        }

    val versionCode: Int
        get() = major * 1_000_000 + minor * 10_000 + patch * 100 + build

    /**
     * Compare this version to another. Returns negative if older,
     * positive if newer, zero if equal (ignoring build metadata).
     */
    fun compareTo(other: Version): Int {
        val majorCmp = major.compareTo(other.major)
        if (majorCmp != 0) return majorCmp
        val minorCmp = minor.compareTo(other.minor)
        if (minorCmp != 0) return minorCmp
        val patchCmp = patch.compareTo(other.patch)
        if (patchCmp != 0) return patchCmp

        // Prerelease versions are older than release versions
        if (prerelease != null && other.prerelease == null) return -1
        if (prerelease == null && other.prerelease != null) return 1
        if (prerelease != null && other.prerelease != null) {
            return prerelease.compareTo(other.prerelease)
        }
        return 0
    }

    companion object {
        /** Parse a version name string into a [Version]. */
        fun parse(name: String): Version {
            val prereleasePart = name.split("-").let { parts ->
                if (parts.size > 1) parts[1] else null
            }
            val mainPart = name.split("-").first()
            val parts = mainPart.split(".")
            val major = parts.getOrElse(0) { "0" }.toIntOrNull() ?: 0
            val minor = parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
            val patch = parts.getOrElse(2) { "0" }.toIntOrNull() ?: 0
            return Version(major, minor, patch, prerelease = prereleasePart)
        }

        /** Current application version. Update on each release. */
        val CURRENT = Version(
            major = 1,
            minor = 0,
            patch = 0,
            build = 1
        )
    }
}

/**
 * Release type for versioning.
 */
enum class ReleaseType(val description: String) {
    ALPHA("Internal testing — unstable"),
    BETA("External testing — feature complete"),
    RC("Release candidate — final validation"),
    STABLE("Production release"),
    HOTFIX("Emergency patch release")
}

/**
 * Pre-defined version constants used throughout the app.
 *
 * NOTE: These values should match the versionCode and versionName
 * defined in app/build.gradle.kts. Update both files together
 * when incrementing the version for a release.
 *
 * Consider reading from PackageManager at runtime to eliminate drift:
 *   context.packageManager.getPackageInfo(context.packageName, 0)
 */
object AppVersions {
    const val MIN_SUPPORTED_VERSION_CODE = 1
    const val MIN_ANDROID_SDK = 26
    const val TARGET_ANDROID_SDK = 37

    /** Current version — keep in sync with app/build.gradle.kts */
    val CURRENT = Version(
        major = 1,
        minor = 0,
        patch = 0,
        build = 1
    )
}
