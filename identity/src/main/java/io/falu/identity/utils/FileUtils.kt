package io.falu.identity.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.CheckResult
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

internal class FileUtils internal constructor(private val context: Context) {

    val internalFileUri: Uri
        get() {
            imageFile.also { return getFileUri(imageFile) }
        }

    fun createFileFromUri(fileUri: Uri, verification: String, imageSide: String? = null): File {
        context.contentResolver.openInputStream(fileUri).use { inputStream ->
            File(context.filesDir, generateFileName(verification, imageSide))
                .let { outputFile ->
                    FileOutputStream(outputFile, false).use { outputStream ->
                        outputStream.write(
                            BitmapFactory.decodeStream(inputStream).toJpgByteArray()
                        )
                    }

                    return outputFile
                }
        }
    }

    private fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.FaluIdentityFileProvider",
            file
        )

    private val imageFile: File
        get() {
            return File.createTempFile(
                "JPEG_${imageFileName}_",
                ".jpg",
                context.filesDir
            )
        }

    private fun generateFileName(verification: String, imageSide: String?): String {
        return "${verification}${imageSide.let { "_$it" }}.jpeg"
    }

    private val imageFileName: String
        get() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    @CheckResult
    private fun Bitmap.toJpgByteArray(quality: Int = 80): ByteArray =
        ByteArrayOutputStream().use {
            this.compress(Bitmap.CompressFormat.JPEG, quality, it)
            it.flush()
            it.toByteArray()
        }

}