package com.bangersoul.aivance.core.data.config

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrityTokenDecoderTest {

    private val decoder = IntegrityTokenDecoder()

    private fun jwsToken(payloadJson: String): String {
        val header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.toByteArray())
        return "$header.$payload.signature"
    }

    @Test
    fun `decodes full verdict payload`() {
        val json = """
            {
              "requestDetails": {
                "requestPackageName": "com.bangersoul.aivance",
                "requestHash": "abc123",
                "timestampMillis": "1700000000000"
              },
              "appIntegrity": {
                "appRecognitionVerdict": "PLAY_RECOGNIZED",
                "packageName": "com.bangersoul.aivance",
                "certificateSha256Digest": ["digest1", "digest2"],
                "versionCode": "42"
              },
              "deviceIntegrity": {
                "deviceRecognitionVerdict": ["MEETS_DEVICE_INTEGRITY"]
              },
              "accountDetails": {
                "appLicensingVerdict": "LICENSED"
              }
            }
        """.trimIndent()

        val payload = decoder.decode(jwsToken(json))
        assertTrue("Valid token should decode", payload != null)

        val verdict = payload!!.toVerdict(now = 1L)
        assertTrue(verdict.deviceIntegrity)
        assertTrue(verdict.appRecognitionVerdict)
        assertTrue(verdict.accountDetails)
        assertEquals(1700000000000L, verdict.timestampMillis)

        val status = payload.toAppStatus(fallbackPackageName = "fallback", fallbackVersionCode = 7L)
        assertTrue(status.isGenuine)
        assertEquals("com.bangersoul.aivance", status.packageName)
        assertEquals(42L, status.versionCode)
        assertEquals("digest1", status.certificateHash)
    }

    @Test
    fun `strong integrity also counts as device-trusted`() {
        val payload = decoder.decode(
            jwsToken("""{"deviceIntegrity":{"deviceRecognitionVerdict":["MEETS_STRONG_INTEGRITY"]}}""")
        )
        assertTrue(payload!!.toVerdict(now = 1L).deviceIntegrity)
    }

    @Test
    fun `absent device verdict means not device-trusted`() {
        val payload = decoder.decode(jwsToken("""{"deviceIntegrity":{}}"""))
        assertFalse(payload!!.toVerdict(now = 1L).deviceIntegrity)
    }

    @Test
    fun `unrecognized app version is not genuine`() {
        val payload = decoder.decode(
            jwsToken("""{"appIntegrity":{"appRecognitionVerdict":"UNRECOGNIZED_VERSION"}}""")
        )
        val status = payload!!.toAppStatus(fallbackPackageName = "fallback", fallbackVersionCode = 1L)
        assertFalse(status.isGenuine)
        assertEquals("fallback", status.packageName)
        assertEquals(1L, status.versionCode)
        assertNull(status.certificateHash)
    }

    @Test
    fun `missing timestamp falls back to provided now`() {
        val payload = decoder.decode(jwsToken("""{"requestDetails":{}}"""))
        assertEquals(123L, payload!!.toVerdict(now = 123L).timestampMillis)
    }

    @Test
    fun `malformed tokens return null`() {
        assertNull(decoder.decode(""))
        assertNull(decoder.decode("not-a-token"))
        assertNull(decoder.decode("header.!!!.signature"))
        // Second segment present but not valid JSON.
        assertNull(decoder.decode("e30.bm90LWpzb24.e30"))
    }

    @Test
    fun `unpadded base64url payload decodes`() {
        // "{}" base64url is "e30" (3 chars, no padding) — decoder must pad it.
        val payload = decoder.decode("e30.e30.e30")
        assertTrue("Unpadded payload should decode", payload != null)
    }

    @Test
    fun `unknown fields in payload are ignored`() {
        val payload = decoder.decode(
            jwsToken("""{"unknownField":true,"environmentDetails":{"playProtectVerdict":"PASS"}}""")
        )
        assertTrue(payload != null)
        assertFalse(payload!!.toVerdict(now = 1L).deviceIntegrity)
    }
}
