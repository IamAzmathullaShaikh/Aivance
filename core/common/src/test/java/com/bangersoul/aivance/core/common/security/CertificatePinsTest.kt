package com.bangersoul.aivance.core.common.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificatePinsTest {

    @Test
    fun `every OkHttp pin is a valid base64 sha256 pin`() {
        CertificatePins.OKHTTP_PINS.values.flatten().forEach { pin ->
            assertTrue("Invalid OkHttp pin format: $pin", CertificatePins.isOkHttpPin(pin))
        }
    }

    @Test
    fun `every hex pin is a 64-char lowercase hex digest`() {
        CertificatePins.HEX_PINS.values.flatten().forEach { pin ->
            assertTrue("Invalid hex pin: $pin", CertificatePins.isHexPin(pin))
        }
    }

    @Test
    fun `no placeholder pins remain`() {
        CertificatePins.HEX_PINS.values.flatten().forEach { pin ->
            assertFalse(
                "Placeholder pin leaked into registry: $pin",
                pin.isBlank() || pin.startsWith("ADD_YOUR_") || pin.startsWith("REPLACE_WITH_") ||
                    pin == "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
            )
        }
        CertificatePins.OKHTTP_PINS.values.flatten().forEach { pin ->
            assertFalse("Placeholder pin leaked into registry: $pin", pin.startsWith("sha256/AAAAAAAA"))
        }
    }

    @Test
    fun `host coverage matches every provider the app calls`() {
        val hosts = CertificatePins.HEX_PINS.keys
        listOf(
            "api.groq.com",
            "api.openai.com",
            "openrouter.ai",
            "api.anthropic.com",
            "generativelanguage.googleapis.com",
            "remoteok.com",
            "remotive.com",
            "api.apify.com",
            "api.hunter.io"
        ).forEach { host ->
            assertTrue("Missing pins for $host", hosts.contains(host))
        }
    }

    @Test
    fun `every host has leaf plus CA backup pins`() {
        CertificatePins.HEX_PINS.forEach { (host, pins) ->
            assertTrue("$host must carry at least 2 pins (leaf + CA)", pins.size >= 2)
        }
    }

    @Test
    fun `hex pins are not fabricated countdown sequences`() {
        // The pre-fix registry contained fabricated descending-countdown hashes
        // (e.g. c7e3f89025e1a38f...b3a2f1e0). A real SPKI digest is uniformly
        // distributed, so a monotonic run over a long window is impossible.
        CertificatePins.HEX_PINS.values.flatten().forEach { pin ->
            val bytes = pin.chunked(2).map { it.toInt(16) }
            val descendingRun = bytes.zipWithNext().count { (a, b) -> a - b == 1 }
            val ascendingRun = bytes.zipWithNext().count { (a, b) -> b - a == 1 }
            assertTrue("Suspicious monotonic pattern in $pin", descendingRun < 24 && ascendingRun < 24)
        }
    }
}
