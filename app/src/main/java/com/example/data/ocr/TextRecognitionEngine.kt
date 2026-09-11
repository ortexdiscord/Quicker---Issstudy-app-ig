package com.example.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream

data class OcrResult(
    val fullText: String,
    val isSuccessful: Boolean,
    val summary: String? = null
)

class TextRecognitionEngine(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeTextFromUri(imageUri: Uri): OcrResult = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            val visionText = recognizer.process(inputImage).await()
            val extracted = visionText.text.trim()

            if (extracted.isNotBlank()) {
                OcrResult(
                    fullText = extracted,
                    isSuccessful = true,
                    summary = generateQuickTakeaway(extracted)
                )
            } else {
                // Fallback attempt with scaled bitmap
                val fallbackText = fallbackBitmapScan(imageUri)
                OcrResult(
                    fullText = fallbackText.ifBlank { "No legible text detected in image." },
                    isSuccessful = fallbackText.isNotBlank(),
                    summary = if (fallbackText.isNotBlank()) generateQuickTakeaway(fallbackText) else null
                )
            }
        } catch (e: Exception) {
            val fallback = fallbackBitmapScan(imageUri)
            OcrResult(
                fullText = fallback.ifBlank { "Text recognition encountered an error: ${e.localizedMessage ?: "Unable to read image."}" },
                isSuccessful = fallback.isNotBlank(),
                summary = if (fallback.isNotBlank()) generateQuickTakeaway(fallback) else null
            )
        }
    }

    private suspend fun fallbackBitmapScan(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(inputImage).await()
                result.text.trim()
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun generateQuickTakeaway(text: String): String {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val bulletPoints = lines.take(4).map { "• $it" }.joinToString("\n")
        return "Key Takeaways:\n$bulletPoints"
    }
}
