package com.bangersoul.aivance.core.network.security

import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Certificate pinning interceptor that validates server certificates
 * against known pins. Provides defense-in-depth beyond standard
 * Android network security config.
 *
 * Pins are SHA-256 hashes of the SubjectPublicKeyInfo.
 * Update these when certificates are rotated.
 *
 * **IMPORTANT**: The default pins are placeholders. Before deploying to
 * production, replace each "REPLACE_WITH_REAL_PIN" with the actual
 * SHA-256 hash of the server's SubjectPublicKeyInfo. Until then,
 * pinning is automatically disabled for hosts with placeholder pins.
 *
 * To generate a real pin:
 * ```
 * openssl s_client -connect hostname:443 -servername hostname </dev/null 2>/dev/null \
 *   | openssl x509 -pubkey -noout \
 *   | openssl pkey -pubin -outform der \
 *   | openssl dgst -sha256 -hex
 * ```
 */
class CertificatePinningInterceptor(
    private val pins: List<PinEntry> = DEFAULT_PINS
) : Interceptor {

    data class PinEntry(
        val hostname: String,
        val sha256Hash: String
    ) {
        /** Returns true if this pin is a placeholder and should be skipped. */
        val isPlaceholder: Boolean get() =
            sha256Hash.isBlank() ||
            sha256Hash.startsWith("ADD_YOUR_") ||
            sha256Hash.startsWith("REPLACE_WITH_") ||
            sha256Hash == "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host

        val matchingPins = pins.filter { it.hostname == host }
        if (matchingPins.isEmpty()) {
            return chain.proceed(request)
        }

        // Skip pinning if ALL pins for this host are placeholders
        if (matchingPins.all { it.isPlaceholder }) {
            return chain.proceed(request)
        }

        val handshake = chain.connection()?.handshake()
            ?: return chain.proceed(request)

        val certs = handshake.peerCertificates
            .filterIsInstance<X509Certificate>()

        if (certs.isEmpty()) {
            throw SSLPeerUnverifiedException("No certificates presented for $host")
        }

        val activePins = matchingPins.filter { !it.isPlaceholder }

        val certPins = certs.map { cert ->
            val der = cert.publicKey.encoded
            val digest = MessageDigest.getInstance("SHA-256").digest(der)
            digest.joinToString("") { "%02x".format(it) }
        }

        val pinFound = certPins.any { certPin ->
            activePins.any { it.sha256Hash.equals(certPin, ignoreCase = true) }
        }

        if (!pinFound) {
            throw CertificateException(
                "Certificate pinning failure for $host. " +
                    "Expected pins: ${activePins.map { it.sha256Hash.take(16) + "..." }}"
            )
        }

        return chain.proceed(request)
    }

    companion object {
        /**
         * Default pins — all placeholders.
         *
         * **BEFORE PRODUCTION RELEASE**, replace each placeholder with a real pin.
         * See class KDoc for instructions on generating pins.
         */
        val DEFAULT_PINS = listOf(
            PinEntry("api.openai.com", "REPLACE_WITH_REAL_PIN"),
            PinEntry("api.groq.com", "REPLACE_WITH_REAL_PIN"),
            PinEntry("openrouter.ai", "REPLACE_WITH_REAL_PIN"),
            PinEntry("remoteok.com", "REPLACE_WITH_REAL_PIN"),
            PinEntry("remotive.com", "REPLACE_WITH_REAL_PIN"),
            PinEntry("api.apify.com", "REPLACE_WITH_REAL_PIN")
        )
    }
}
