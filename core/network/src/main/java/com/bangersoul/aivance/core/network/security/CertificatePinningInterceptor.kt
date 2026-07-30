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
 */
class CertificatePinningInterceptor(
    private val pins: List<PinEntry> = DEFAULT_PINS
) : Interceptor {

    data class PinEntry(
        val hostname: String,
        val sha256Hash: String
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host

        val matchingPins = pins.filter { it.hostname == host }
        if (matchingPins.isEmpty()) {
            // Host not pinned; allow connection
            return chain.proceed(request)
        }

        val handshake = chain.connection()?.handshake()
            ?: return chain.proceed(request)

        val certs = handshake.peerCertificates
            .filterIsInstance<X509Certificate>()

        if (certs.isEmpty()) {
            throw SSLPeerUnverifiedException("No certificates presented for $host")
        }

        // Check each certificate's SPKI fingerprint against known pins
        val certPins = certs.map { cert ->
            val der = cert.publicKey.encoded
            val digest = MessageDigest.getInstance("SHA-256").digest(der)
            val pin = digest.joinToString("") { "%02x".format(it) }
            pin
        }

        val pinFound = certPins.any { certPin ->
            matchingPins.any { entry ->
                entry.sha256Hash.equals(certPin, ignoreCase = true)
            }
        }

        if (!pinFound) {
            throw CertificateException(
                "Certificate pinning failure for $host. " +
                    "Expected pins: ${matchingPins.map { it.sha256Hash.take(16) + "..." }}"
            )
        }

        return chain.proceed(request)
    }

    companion object {
        val DEFAULT_PINS = listOf(
            // OpenAI API
            PinEntry("api.openai.com", "ADD_YOUR_PIN_HERE"),
            // Groq API
            PinEntry("api.groq.com", "ADD_YOUR_PIN_HERE"),
            // OpenRouter
            PinEntry("openrouter.ai", "ADD_YOUR_PIN_HERE"),
            // RemoteOK
            PinEntry("remoteok.com", "ADD_YOUR_PIN_HERE"),
            // Remotive
            PinEntry("remotive.com", "ADD_YOUR_PIN_HERE"),
            // Apify
            PinEntry("api.apify.com", "ADD_YOUR_PIN_HERE")
        )
    }
}
