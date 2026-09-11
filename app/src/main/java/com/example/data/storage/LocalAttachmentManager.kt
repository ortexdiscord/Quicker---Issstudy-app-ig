package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.data.ocr.TextRecognitionEngine
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

data class LocalSourceAttachment(
    val localUri: String,
    val fileName: String,
    val fileType: String, // "pdf", "image", "doc"
    val fileSizeFormatted: String,
    val extractedText: String,
    val pageCount: Int = 1
)

class LocalAttachmentManager(private val context: Context) {

    private val textRecognitionEngine = TextRecognitionEngine(context)
    private val mlKitRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val mediaDir: File by lazy {
        val dir = File(context.filesDir, "quicks_local_media")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    /**
     * Copies any selected URI (from photo picker or document picker)
     * exclusively into the local app storage directory.
     * ZERO bytes are ever uploaded to remote cloud/Firebase storage.
     */
    suspend fun saveAndProcessLocalAttachment(sourceUri: Uri): Result<LocalSourceAttachment> = withContext(Dispatchers.IO) {
        try {
            val fileName = queryFileName(sourceUri)
            val lowerName = fileName.lowercase()
            val extension = when {
                lowerName.endsWith(".pdf") -> "pdf"
                lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") -> "image"
                lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".csv") -> "doc"
                else -> {
                    val mime = context.contentResolver.getType(sourceUri) ?: ""
                    if (mime.contains("pdf")) "pdf"
                    else if (mime.startsWith("image/")) "image"
                    else "doc"
                }
            }

            val targetFile = File(mediaDir, "attachment_${System.currentTimeMillis()}_${sanitizeFileName(fileName)}")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Failed to read selected file."))

            val fileSize = formatFileSize(targetFile.length())
            val localUri = Uri.fromFile(targetFile).toString()

            var extractedText = ""
            var pageCount = 1

            when (extension) {
                "pdf" -> {
                    val (pdfText, pages) = extractTextFromLocalPdf(targetFile)
                    extractedText = pdfText
                    pageCount = pages
                }
                "image" -> {
                    val ocr = textRecognitionEngine.recognizeTextFromUri(Uri.fromFile(targetFile))
                    extractedText = ocr.fullText
                }
                else -> {
                    // Plain text or markdown
                    extractedText = try {
                        targetFile.readText(Charsets.UTF_8).take(8000)
                    } catch (_: Exception) {
                        "File attached: $fileName"
                    }
                }
            }

            Result.success(
                LocalSourceAttachment(
                    localUri = localUri,
                    fileName = fileName,
                    fileType = extension,
                    fileSizeFormatted = fileSize,
                    extractedText = extractedText,
                    pageCount = pageCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts text from each page of a PDF using Android's native PdfRenderer
     * combined with on-device ML Kit OCR.
     */
    private suspend fun extractTextFromLocalPdf(file: File): Pair<String, Int> = withContext(Dispatchers.IO) {
        val stringBuilder = StringBuilder()
        var totalPages = 1

        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            totalPages = renderer.pageCount
            val pagesToProcess = minOf(totalPages, 5) // Process first 5 pages for speed and high yield

            for (i in 0 until pagesToProcess) {
                val page = renderer.openPage(i)
                val width = page.width * 2 // 2x scale for crisp OCR recognition
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val inputImage = InputImage.fromBitmap(bitmap, 0)
                try {
                    val visionText = mlKitRecognizer.process(inputImage).await()
                    val pageText = visionText.text.trim()
                    if (pageText.isNotBlank()) {
                        stringBuilder.appendLine("--- Page ${i + 1} ---")
                        stringBuilder.appendLine(pageText)
                        stringBuilder.appendLine()
                    }
                } catch (_: Exception) {}

                bitmap.recycle()
            }

            renderer.close()
            fileDescriptor.close()
        } catch (_: Exception) {}

        val resultText = stringBuilder.toString().trim()
        val finalText = if (resultText.isNotBlank()) {
            resultText
        } else {
            "PDF document: ${file.name} ($totalPages pages). On-device preview ready."
        }

        Pair(finalText, totalPages)
    }

    private fun queryFileName(uri: Uri): String {
        var name = "attachment"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val fetched = cursor.getString(nameIndex)
                    if (!fetched.isNullOrBlank()) {
                        name = fetched
                    }
                }
            }
        } catch (_: Exception) {}

        if (name == "attachment") {
            val path = uri.lastPathSegment
            if (!path.isNullOrBlank()) {
                name = path.substringAfterLast("/")
            }
        }
        return name
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val clampedIndex = digitGroups.coerceIn(0, units.size - 1)
        val value = sizeInBytes / Math.pow(1024.0, clampedIndex.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[clampedIndex]
    }
}
