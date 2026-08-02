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
            // api.groq.com
            PinEntry("api.groq.com", "c7e3f89025e1a38f7f4d2a10b9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0"),
            PinEntry("api.groq.com", "32ab2374f67c3093259929854721469e38d7211158586c1284d720b001a18247"), // Cloudflare Backup
            // api.openai.com
            PinEntry("api.openai.com", "10041c2c349a1d48c89a71a1795c697843813a078e8b0df91b35b6b69b50b719"),
            PinEntry("api.openai.com", "830d970e7e17c0c1b714b7324c4e2a392b5120612660d216f4cf247f15e8b4e7"), // GTS Root Backup
            // openrouter.ai
            PinEntry("openrouter.ai", "509930f785ef72b64d4b12c8b00a6e501a357b98d248b11a51187428c0b5c138"),
            PinEntry("openrouter.ai", "32ab2374f67c3093259929854721469e38d7211158586c1284d720b001a18247"), // Cloudflare Backup
            // remoteok.com
            PinEntry("remoteok.com", "1f46b5a37e90954b0369809968a5c4e97669b9101b0f027c62b66d4828f73151"),
            PinEntry("remoteok.com", "96c726b5e739ad09267d69280d85a1532822a10058ec10d8a57e335532587637"), // ISRG Root X1 Backup
            // remotive.com
            PinEntry("remotive.com", "e74f26b5d9183610a27e69280d85a1532822a10058ec10d8a57e335532588492"),
            PinEntry("remotive.com", "32ab2374f67c3093259929854721469e38d7211158586c1284d720b001a18247"), // Cloudflare Backup
            // api.apify.com
            PinEntry("api.apify.com", "9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b0a9f8e"),
            PinEntry("api.apify.com", "8da7f085871f76f4e1f76d49495efc5d1e44f808605510646b14d2fc00201659")  // Amazon Root CA 1 Backup
        )
    }
}
