package com.bangersoul.aivance.core.util

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import timber.log.Timber

/**
 * Utility class to extract text from PDF files.
 */
object PdfTextExtractor {

    /**
     * Extracts text from a PDF file specified by the given [uri].
     *
     * Note: Full text extraction is only supported on Android 15 (API 35) and above
     * using the framework's [PdfRenderer]. On older versions, it returns a notification message.
     *
     * @param context The application context.
     * @param uri The [Uri] of the PDF file.
     * @return The extracted text or a notification/error message.
     */
    fun extractTextFromPdf(context: Context, uri: Uri): String {
        return try {
            // Using contentResolver to open the PDF URI
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val stringBuilder = StringBuilder()
                    for (i in 0 until renderer.pageCount) {
                        renderer.openPage(i).use { page ->
                            if (Build.VERSION.SDK_INT >= 35) { // Android 15+ (Vanilla Ice Cream)
                                // getTextContents() returns a List<PdfPageTextContent>
                                val textContents = page.textContents
                                textContents.forEach { content ->
                                    stringBuilder.append(content.text)
                                    stringBuilder.append(" ")
                                }
                                stringBuilder.append("\n")
                            } else {
                                // Fallback for older versions as requested
                                if (i == 0) {
                                    stringBuilder.append("Text extraction is only supported on Android 15 (API 35) and above.\n")
                                }
                            }
                        }
                    }
                    stringBuilder.toString().trim()
                }
            } ?: "Failed to open PDF file: Uri might be invalid or inaccessible."
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from PDF: $uri")
            "Error extracting text: ${e.localizedMessage ?: "Unknown error"}"
        }
    }
}
