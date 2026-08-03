package com.bangersoul.aivance.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSecurityTest {

    @Test
    fun `encrypt then decrypt round-trips`() {
        val plain = """{"resumes":[{"id":1,"text":"My résumé content"}],"jobs":[]}"""
        val bytes = BackupSecurity.encryptString(plain, "correct horse battery staple")
        val decrypted = BackupSecurity.decryptBytes(bytes, "correct horse battery staple")
        assertEquals(plain, decrypted)
    }

    @Test
    fun `wrong passphrase fails decryption`() {
        val bytes = BackupSecurity.encryptString("secret", "right-passphrase")
        assertThrows(Exception::class.java) {
            BackupSecurity.decryptBytes(bytes, "wrong-passphrase")
        }
    }

    @Test
    fun `file has AVB1 magic header`() {
        val bytes = BackupSecurity.encryptString("x", "p")
        assertEquals("AVB1", String(bytes.copyOfRange(0, 4), Charsets.US_ASCII))
        // salt (16) + iv (12) after the 4-byte magic
        assertTrue(bytes.size > 4 + 16 + 12)
    }

    @Test
    fun `rejects non-AVB1 input`() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupSecurity.decryptBytes(ByteArray(64) { 0x42 }, "p")
        }
    }

    @Test
    fun `rejects truncated input`() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupSecurity.decryptBytes(ByteArray(8), "p")
        }
    }

    @Test
    fun `random salt means same plaintext encrypts differently each time`() {
        val a = BackupSecurity.encryptString("same text", "p")
        val b = BackupSecurity.encryptString("same text", "p")
        assertNotEquals(
            "Two exports of the same payload must differ (fresh salt + IV per file)",
            a.toList(),
            b.toList()
        )
    }

    @Test
    fun `empty plaintext still yields a valid file`() {
        val bytes = BackupSecurity.encryptString("", "p")
        assertEquals("", BackupSecurity.decryptBytes(bytes, "p"))
    }
}
