package com.bangersoul.aivance.core.network.security

import com.bangersoul.aivance.core.common.security.CertificatePins
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Certificate pinning interceptor that validates server certificates
 * against known pins. Provides defense-in-depth beyond OkHttp's native
 * [okhttp3.CertificatePinner] (which runs at handshake time).
 *
 * Pins are SHA-256 hashes of the SubjectPublicKeyInfo (SPKI), sourced from
 * [CertificatePins] — a registry of **live-verified** pins (leaf + issuing
 * CA + root CA per host, measured from the real TLS chains on 2026-08-04).
 *
 * NOTE: as an application interceptor this runs after the connection is
 * established; the authoritative enforcement is OkHttp's native
 * CertificatePinner wired in NetworkModule and the provider clients.
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
         * Real pins sourced from [CertificatePins.HEX_PINS] — live-verified
         * SPKI SHA-256 hashes (leaf + CA backups) for every provider host.
         * Generated from the live TLS chains on 2026-08-04 (extract_pins.py).
         */
        val DEFAULT_PINS: List<PinEntry> = CertificatePins.HEX_PINS
            .flatMap { (host, hashes) -> hashes.map { PinEntry(host, it) } }
    }
}
