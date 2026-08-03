package com.bangersoul.aivance.core.database.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Proves the fail-closed contract (audit S-03): [EncryptionService] must NEVER
 * return the plaintext input unchanged, and must never return the ciphertext
 * unchanged from [decrypt]. In an environment where Tink cannot initialize, the
 * correct behavior is to throw — silently returning plaintext is a release blocker.
 *
 * This test is environment-agnostic by design: it passes whether Tink succeeds
 * (ciphertext != plaintext, round-trips) OR fails (throws instead of leaking).
 */
@RunWith(RobolectricTestRunner::class)
class EncryptionServiceFailClosedTest {

    private fun service(): EncryptionService {
        return EncryptionService(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `encrypt never returns plaintext`() {
        val svc = service()
        val secret = "sk-live-abcdef0123456789"
        val result = runCatching { svc.encrypt(secret) }
        if (result.isSuccess) {
            val cipher = result.getOrThrow()
            assertNotEquals("encrypt() must not return plaintext", secret, cipher)
            // Round-trip works when crypto is available.
            assertEquals(secret, svc.decrypt(cipher))
        } else {
            // Fail-closed: throwing is the correct behavior when crypto is unavailable.
            // Returning `secret` unchanged would be a silent security downgrade.
            assertNotEquals(
                "encrypt() must not swallow failure into plaintext",
                secret,
                result.exceptionOrNull()?.message
            )
        }
    }

    @Test
    fun `empty input is a safe no-op`() {
        val svc = service()
        assertEquals("", svc.encrypt(""))
        assertEquals("", svc.decrypt(""))
    }

    @Test
    fun `decrypt never returns ciphertext as-is`() {
        val svc = service()
        // A garbage value must either throw or fail — never be echoed back.
        val garbage = "not-a-real-ciphertext"
        runCatching { svc.decrypt(garbage) }
            .onFailure { return } // fail-closed: throw is acceptable
            .onSuccess { decrypted ->
                // If it "succeeded" it must not be the untouched input unless Tink
                // somehow accepts it; asserting the security property that matters:
                fail("decrypt() must not echo ciphertext back as plaintext: $decrypted")
            }
    }

    @Test
    fun `tampered ciphertext fails closed`() {
        val svc = service()
        val plain = "attack-at-dawn"
        val cipher = runCatching { svc.encrypt(plain) }.getOrNull() ?: return // env-dependent
        val tampered = cipher.dropLast(1) + if (cipher.last() == 'A') 'B' else 'A'
        runCatching { svc.decrypt(tampered) }
            .onFailure { return }
            .onSuccess { decrypted ->
                assertNotEquals("tampered ciphertext must not decrypt to original", plain, decrypted)
            }
    }
}
