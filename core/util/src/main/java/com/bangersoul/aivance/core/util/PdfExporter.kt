package com.bangersoul.aivance.core.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Renders text (with optional header→body sections) to a real PDF using the
 * built-in [PdfDocument] API — no heavyweight renderer needed.
 *
 * The resulting file is written to the app cache directory and exposed through
 * the app's FileProvider (`${packageName}.fileprovider`) so callers can share
 * it via an ACTION_SEND intent or open it with a viewer.
 */
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * @param title   Rendered as the document heading (bold, larger).
     * @param content Plain text body — used when [sections] is null.
     * @param sections header → body pairs rendered with a bold header per block.
     * @return [Result.Success] with a content:// [Uri] or [Result.Failure].
     */
    suspend fun exportToPdf(
        title: String,
        content: String? = null,
        sections: List<Pair<String, String>>? = null
    ): CoreResult<Uri> = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        try {
            val pageWidth = 595  // A4 width in points
            val pageHeight = 842 // A4 height
            val margin = 72f     // 1-inch margins
            val maxWidth = pageWidth - 2 * margin

            val paint = Paint().apply {
                textSize = 12f
                color = Color.BLACK
                isAntiAlias = true
            }
            val titlePaint = Paint(paint).apply {
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
            }
            val headerPaint = Paint(paint).apply {
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPos = margin

            // Finish the current page and start a fresh one when the cursor
            // would overflow the bottom margin.
            fun ensureSpace(needed: Float) {
                if (yPos + needed > pageHeight - margin) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = margin
                }
            }

            // Word-wraps [text] onto the canvas, breaking lines before they
            // exceed maxWidth and adding paragraph spacing between \n blocks.
            fun drawWrapped(text: String, textPaint: Paint, lineSpacing: Float, paragraphSpacing: Float) {
                text.split("\n").forEach { paragraph ->
                    val line = StringBuilder()
                    paragraph.split(" ").forEach { word ->
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        if (textPaint.measureText(testLine) > maxWidth) {
                            ensureSpace(lineSpacing)
                            canvas.drawText(line.toString(), margin, yPos, textPaint)
                            yPos += lineSpacing
                            line.setLength(0)
                            line.append(word)
                        } else {
                            line.setLength(0)
                            line.append(testLine)
                        }
                    }
                    if (line.isNotEmpty()) {
                        ensureSpace(lineSpacing)
                        canvas.drawText(line.toString(), margin, yPos, textPaint)
                        yPos += lineSpacing
                    }
                    yPos += paragraphSpacing
                }
            }

            // Title
            ensureSpace(30f)
            canvas.drawText(title, margin, yPos, titlePaint)
            yPos += 30f

            if (sections != null) {
                sections.forEach { (header, body) ->
                    ensureSpace(40f)
                    canvas.drawText(header, margin, yPos, headerPaint)
                    yPos += 20f
                    drawWrapped(body, paint, 18f, 8f)
                    yPos += 10f
                }
            } else {
                drawWrapped(content.orEmpty(), paint, 18f, 8f)
            }

            document.finishPage(page)

            val safeTitle = title.replace(Regex("[^A-Za-z0-9_-]+"), "_").ifBlank { "export" }
            val fileName = "${safeTitle}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.Success(uri)
        } catch (e: Exception) {
            Timber.e(e, "PDF export failed")
            Result.Failure(DomainError(e.message ?: "PDF export failed", e))
        } finally {
            // Always release the native PdfDocument resources — even when a
            // mid-write exception left an unfinished page (writeTo throws on
            // disk-full, for example).
            document.close()
        }
    }
}
