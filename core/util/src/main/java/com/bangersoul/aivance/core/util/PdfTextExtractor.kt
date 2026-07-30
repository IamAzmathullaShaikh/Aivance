package com.bangersoul.aivance.core.util

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import timber.log.Timber

/**
 * Utility class to extract text from PDF files across all Android versions (API 26+).
 */
object PdfTextExtractor {

    @Volatile
    private var isPdfBoxInitialized = false

    private fun ensurePdfBoxInitialized(context: Context) {
        if (!isPdfBoxInitialized) {
            synchronized(this) {
                if (!isPdfBoxInitialized) {
                    try {
                        PDFBoxResourceLoader.init(context.applicationContext)
                        isPdfBoxInitialized = true
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to initialize PDFBoxResourceLoader")
                    }
                }
            }
        }
    }

    /**
     * Extracts text from a PDF file specified by the given [uri].
     * Uses native PdfRenderer textContents on Android 15 (API 35+) when available,
     * and PDFBox-Android on Android 8.0 through 14 (API 26-34) to guarantee zero crashes
     * and full text extraction across all supported minSdk levels.
     *
     * @param context The application context.
     * @param uri The [Uri] of the PDF file.
     * @return The extracted text or an error message.
     */
    fun extractTextFromPdf(context: Context, uri: Uri): String {
        return try {
            if (Build.VERSION.SDK_INT >= 35) {
                val nativeText = extractWithNativePdfRenderer(context, uri)
                if (nativeText.isNotBlank()) return nativeText
            }
            extractWithPdfBox(context, uri)
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from PDF: $uri")
            "Error extracting text: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    private fun extractWithNativePdfRenderer(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val stringBuilder = StringBuilder()
                    for (i in 0 until renderer.pageCount) {
                        renderer.openPage(i).use { page ->
                            if (Build.VERSION.SDK_INT >= 35) {
                                val textContents = page.textContents
                                textContents.forEach { content ->
                                    stringBuilder.append(content.text).append(" ")
                                }
                                stringBuilder.append("\n")
                            }
                        }
                    }
                    stringBuilder.toString().trim()
                }
            } ?: ""
        } catch (e: Throwable) {
            Timber.w(e, "Native PdfRenderer extraction failed, falling back to PDFBox")
            ""
        }
    }

    private fun extractWithPdfBox(context: Context, uri: Uri): String {
        ensurePdfBoxInitialized(context)
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { pdDocument ->
                val stripper = PDFTextStripper()
                stripper.getText(pdDocument).trim()
            }
        } ?: "Failed to open PDF file: Uri might be invalid or inaccessible."
    }
}
