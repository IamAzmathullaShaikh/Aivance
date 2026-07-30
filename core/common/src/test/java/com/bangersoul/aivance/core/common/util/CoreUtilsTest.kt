package com.bangersoul.aivance.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreUtilsTest {

    @Test
    fun dateUtils_iso8601FormattingAndParsing_roundTrips() {
        val now = System.currentTimeMillis()
        val isoStr = DateUtils.formatIso8601(now)
        assertNotNull(isoStr)
        assertTrue(isoStr.endsWith("Z"))

        val parsed = DateUtils.parseIso8601(isoStr)
        assertNotNull(parsed)
    }

    @Test
    fun dateUtils_getElapsedTime_returnsExpectedStrings() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", DateUtils.getElapsedTime(now, now))
        assertEquals("5m ago", DateUtils.getElapsedTime(now - 5 * 60 * 1000L, now))
        assertEquals("2h ago", DateUtils.getElapsedTime(now - 2 * 3600 * 1000L, now))
        assertEquals("3d ago", DateUtils.getElapsedTime(now - 3 * 24 * 3600 * 1000L, now))
    }

    @Test
    fun idGenerator_uuidAndPrefixedId_areValidAndUnique() {
        val uuid = IdGenerator.generateUuid()
        assertNotNull(uuid)
        assertEquals(36, uuid.length)

        val prefixedId = IdGenerator.generatePrefixedId("Job_Listing!")
        assertTrue(prefixedId.startsWith("joblisting_"))
    }

    @Test
    fun hashUtils_sha256AndMd5_produceCorrectHashLengths() {
        val sha256Hash = HashUtils.sha256("test input")
        assertEquals(64, sha256Hash.length)

        val md5Hash = HashUtils.md5("test input")
        assertEquals(32, md5Hash.length)
    }

    @Test
    fun formatUtils_fileSizeAndTextTruncation_formatCorrectly() {
        assertEquals("0 B", FormatUtils.formatFileSize(0))
        assertEquals("1.0 KB", FormatUtils.formatFileSize(1024))
        assertEquals("1.5 MB", FormatUtils.formatFileSize((1.5 * 1024 * 1024).toLong()))

        assertEquals("Short", FormatUtils.truncateText("Short", 10))
        assertEquals("Hello...", FormatUtils.truncateText("Hello World Example", 8))
    }

    @Test
    fun jsonUtils_isValidJson_validatesCorrectly() {
        assertTrue(JsonUtils.isValidJson("""{"key": "value"}"""))
        assertFalse(JsonUtils.isValidJson("invalid json"))
    }

    @Test
    fun fileUtils_extensionMimeTypeAndSanitize_workAsExpected() {
        assertEquals("pdf", FileUtils.getFileExtension("document.pdf"))
        assertEquals("application/pdf", FileUtils.getMimeType("resume.pdf"))
        assertEquals("text/plain", FileUtils.getMimeType("notes.txt"))

        assertEquals("My_Resume_2026_.pdf", FileUtils.sanitizeFileName("My Resume#2026!.pdf"))
    }

    @Test
    fun uriUtils_isValidUriAndQueryParam_extractCorrectly() {
        assertTrue(UriUtils.isValidUri("https://example.com/search?q=android"))
        assertFalse(UriUtils.isValidUri(""))

        val param = UriUtils.extractQueryParam("https://example.com/search?q=android&page=2", "q")
        assertEquals("android", param)
        assertNull(UriUtils.extractQueryParam("https://example.com/search?q=android", "missing"))
    }

    @Test
    fun resource_loadingSuccessError_states() {
        val loading: Resource<String> = Resource.Loading
        assertTrue(loading.isLoading)

        val success: Resource<String> = Resource.Success("Data")
        assertTrue(success.isSuccess)
        assertEquals("Data", (success as Resource.Success).data)

        val error: Resource<String> = Resource.Error(IllegalStateException("Failed"))
        assertTrue(error.isError)
        assertEquals("Failed", (error as Resource.Error).exception.message)
    }
}
