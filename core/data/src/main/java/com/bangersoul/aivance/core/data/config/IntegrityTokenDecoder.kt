package com.bangersoul.aivance.core.data.config

import com.bangersoul.aivance.core.domain.config.PlayIntegrityManager
import java.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Decodes the plain-text JSON payload of a Play Integrity verdict token.
 *
 * The standard integrity token is a JWS (`header.payload.signature`); the middle
 * section is base64url-encoded JSON — see
 * developer.android.com/google/play/integrity/verdicts. The payload is readable
 * client-side, but the token's signature can only be verified server-side (via
 * the Google Play Developer API), so verdicts read here must not gate
 * high-security operations on their own.
 *
 * Pure Kotlin (no Android imports) so it can be unit-tested on the JVM.
 */
internal class IntegrityTokenDecoder(
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    /** Decodes the token's verdict payload, or `null` when the token is malformed. */
    fun decode(token: String): IntegrityTokenPayload? {
        return try {
            val parts = token.split('.')
            if (parts.size < 2) return null
            val payload = parts[1]
            // JWT payloads are base64url without padding; java.util.Base64's URL
            // decoder requires padding, so restore it before decoding.
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val bytes = Base64.getUrlDecoder().decode(padded)
            json.decodeFromString<IntegrityTokenPayload>(bytes.decodeToString())
        } catch (e: Exception) {
            null
        }
    }
}

// ── Verdict mapping (pure functions) ─────────────────────────────────────────

internal fun IntegrityTokenPayload.toVerdict(now: Long): PlayIntegrityManager.IntegrityVerdict =
    PlayIntegrityManager.IntegrityVerdict(
        deviceIntegrity = deviceIntegrity?.deviceRecognitionVerdict
            ?.any { it in DEVICE_OR_STRONG_INTEGRITY } == true,
        appRecognitionVerdict = appIntegrity?.appRecognitionVerdict == PLAY_RECOGNIZED,
        accountDetails = accountDetails?.appLicensingVerdict == LICENSED,
        timestampMillis = requestDetails?.timestampMillis?.toLongOrNull() ?: now
    )

internal fun IntegrityTokenPayload.toAppStatus(
    fallbackPackageName: String,
    fallbackVersionCode: Long
): PlayIntegrityManager.AppIntegrityStatus = PlayIntegrityManager.AppIntegrityStatus(
    isGenuine = appIntegrity?.appRecognitionVerdict == PLAY_RECOGNIZED,
    packageName = appIntegrity?.packageName ?: fallbackPackageName,
    versionCode = appIntegrity?.versionCode?.toLongOrNull() ?: fallbackVersionCode,
    certificateHash = appIntegrity?.certificateSha256Digest?.firstOrNull()
)

internal const val PLAY_RECOGNIZED = "PLAY_RECOGNIZED"
internal const val LICENSED = "LICENSED"

private val DEVICE_OR_STRONG_INTEGRITY = setOf("MEETS_DEVICE_INTEGRITY", "MEETS_STRONG_INTEGRITY")

// ── Verdict payload models (subset of the documented payload) ───────────────

@Serializable
internal data class IntegrityTokenPayload(
    @SerialName("requestDetails") val requestDetails: RequestDetails? = null,
    @SerialName("appIntegrity") val appIntegrity: AppIntegrity? = null,
    @SerialName("deviceIntegrity") val deviceIntegrity: DeviceIntegrity? = null,
    @SerialName("accountDetails") val accountDetails: AccountDetails? = null
)

@Serializable
internal data class RequestDetails(
    @SerialName("requestPackageName") val requestPackageName: String? = null,
    @SerialName("requestHash") val requestHash: String? = null,
    @SerialName("timestampMillis") val timestampMillis: String? = null
)

@Serializable
internal data class AppIntegrity(
    @SerialName("appRecognitionVerdict") val appRecognitionVerdict: String? = null,
    @SerialName("packageName") val packageName: String? = null,
    @SerialName("certificateSha256Digest") val certificateSha256Digest: List<String>? = null,
    @SerialName("versionCode") val versionCode: String? = null
)

@Serializable
internal data class DeviceIntegrity(
    @SerialName("deviceRecognitionVerdict") val deviceRecognitionVerdict: List<String>? = null
)

@Serializable
internal data class AccountDetails(
    @SerialName("appLicensingVerdict") val appLicensingVerdict: String? = null
)
