package com.bangersoul.aivance.core.util

import android.content.Context
import android.net.Uri
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import timber.log.Timber

/**
 * Utility class to extract text from DOCX files using Apache POI.
 */
object DocxTextExtractor {

    /**
     * Extracts text from a DOCX file specified by the given [uri].
     *
     * @param context The application context.
     * @param uri The [Uri] of the DOCX file.
     * @return The extracted text or an error message.
     */
    fun extractTextFromDocx(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                XWPFDocument(inputStream).use { document ->
                    XWPFWordExtractor(document).use { extractor ->
                        extractor.text.trim()
                    }
                }
            } ?: "Failed to open DOCX file: Uri might be invalid."
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from DOCX: $uri")
            "Error extracting text: ${e.localizedMessage ?: "Unknown error"}"
        }
    }
}
