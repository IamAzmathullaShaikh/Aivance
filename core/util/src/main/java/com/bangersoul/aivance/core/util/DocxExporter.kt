package com.bangersoul.aivance.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocxExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportToDocx(
        title: String,
        sections: List<Pair<String, String>>
    ): CoreResult<Uri> = withContext(Dispatchers.IO) {
        try {
            val doc = XWPFDocument()

            // Title paragraph
            val titlePara = doc.createParagraph()
            val titleRun = titlePara.createRun()
            titleRun.isBold = true
            titleRun.fontSize = 18
            titleRun.setText(title)

            // Section paragraphs
            sections.forEach { (header, body) ->
                val sectionHeaderPara = doc.createParagraph()
                val headerRun = sectionHeaderPara.createRun()
                headerRun.isBold = true
                headerRun.fontSize = 14
                headerRun.setText(header)

                val bodyPara = doc.createParagraph()
                body.split("\n").forEachIndexed { index, line ->
                    val run = bodyPara.createRun()
                    if (index > 0) run.addCarriageReturn()
                    run.setText(line)
                }
            }

            val safeTitle = title.replace(Regex("[^A-Za-z0-9_-]+"), "_").ifBlank { "export" }
            val fileName = "${safeTitle}_${System.currentTimeMillis()}.docx"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { doc.write(it) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.Success(uri)
        } catch (e: Exception) {
            Timber.e(e, "DOCX export failed")
            Result.Failure(DomainError(e.message ?: "DOCX export failed", e))
        }
    }
}
