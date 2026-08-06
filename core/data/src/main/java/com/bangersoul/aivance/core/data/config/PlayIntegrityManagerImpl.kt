package com.bangersoul.aivance.core.data.config

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.IntegrityError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.util.Clock
import com.bangersoul.aivance.core.domain.config.PlayIntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.model.IntegrityErrorCode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Production [PlayIntegrityManager] backed by the Google Play Integrity SDK.
 *
 * Requests a standard integrity token via [IntegrityManagerFactory] and decodes
 * its plain-text JSON verdict payload locally (see [IntegrityTokenDecoder]).
 * Degrades gracefully to [Result.Failure] whenever the SDK cannot run — Play
 * Services/Play Store missing, API unavailable, transient network errors — so
 * callers never crash on unsupported devices.
 */
@Singleton
class PlayIntegrityManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock
) : PlayIntegrityManager {

    private val tokenDecoder = IntegrityTokenDecoder()

    private val integrityManager by lazy {
        // May throw on devices without Play Services; created lazily inside the
        // requestToken() try/catch so it maps to a graceful Failure instead.
        IntegrityManagerFactory.create(context)
    }

    override suspend fun verifyIntegrity(): CoreResult<PlayIntegrityManager.IntegrityVerdict> {
        return when (val tokenResult = requestToken()) {
            is Result.Success -> {
                val payload = tokenDecoder.decode(tokenResult.data)
                if (payload == null) {
                    Result.Failure(
                        IntegrityError.InternalError(
                            IllegalStateException("Failed to decode integrity token payload")
                        )
                    )
                } else {
                    Result.Success(payload.toVerdict(now = clock.now()))
                }
            }
            is Result.Failure -> tokenResult
        }
    }

    override suspend fun verifyAppIntegrity(): CoreResult<PlayIntegrityManager.AppIntegrityStatus> {
        return when (val tokenResult = requestToken()) {
            is Result.Success -> {
                val payload = tokenDecoder.decode(tokenResult.data)
                if (payload == null) {
                    Result.Failure(
                        IntegrityError.InternalError(
                            IllegalStateException("Failed to decode integrity token payload")
                        )
                    )
                } else {
                    Result.Success(
                        payload.toAppStatus(
                            fallbackPackageName = context.packageName,
                            fallbackVersionCode = localVersionCode()
                        )
                    )
                }
            }
            is Result.Failure -> tokenResult
        }
    }

    private suspend fun requestToken(): CoreResult<String> {
        return try {
            val request = IntegrityTokenRequest.builder()
                .setNonce(newNonce())
                .build()
            val token = integrityManager.requestIntegrityToken(request).await().token()
            Result.Success(token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Failure(toIntegrityError(e))
        }
    }

    private fun toIntegrityError(e: Exception): IntegrityError {
        if (e is IntegrityServiceException) {
            return when (e.errorCode) {
                // Transient/network conditions — safe to retry later.
                IntegrityErrorCode.NETWORK_ERROR,
                IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE,
                IntegrityErrorCode.TOO_MANY_REQUESTS,
                IntegrityErrorCode.CLIENT_TRANSIENT_ERROR -> IntegrityError.NetworkError(e)

                // The device environment cannot run the Integrity API at all.
                IntegrityErrorCode.PLAY_SERVICES_NOT_FOUND,
                IntegrityErrorCode.PLAY_SERVICES_VERSION_OUTDATED,
                IntegrityErrorCode.PLAY_STORE_NOT_FOUND,
                IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED,
                IntegrityErrorCode.PLAY_STORE_ACCOUNT_NOT_FOUND,
                IntegrityErrorCode.API_NOT_AVAILABLE,
                IntegrityErrorCode.CANNOT_BIND_TO_SERVICE -> IntegrityError.PlayServicesNotAvailable(e)

                else -> IntegrityError.InternalError(e)
            }
        }
        return IntegrityError.InternalError(e)
    }

    private fun newNonce(): String {
        // Base64url-encoded random nonce (>= 16 bytes); the SDK rejects
        // non-base64 nonces (NONCE_IS_NOT_BASE64) and short ones (NONCE_TOO_SHORT).
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun localVersionCode(): Long = runCatching {
        PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(context.packageName, 0)
        )
    }.getOrNull() ?: 0L
}
