package com.bangersoul.aivance.core.util

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// API 35 is not resolvable by the bundled Robolectric (UnknownSdk); the PDFBox path
// exercised here is the fallback used on all devices below Android 15.
@Config(sdk = [28, 34])
class PdfTextExtractorTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun extractTextFromPdf_handlesInvalidUri_returnsErrorMessage() {
        val uri = Uri.parse("content://com.bangersoul.aivance.test/invalid.pdf")
        val result = PdfTextExtractor.extractTextFromPdf(context, uri)
        assertNotNull(result)
        assertTrue(result.contains("Failed") || result.contains("Error"))
    }
}
