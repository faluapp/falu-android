package io.falu.identity.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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

    fun createFileFromInputStream(stream: InputStream, fileName: String): File {
        stream.use { inputStream ->
            File(context.filesDir, fileName)
                .let { outputFile ->
                    FileOutputStream(outputFile, false).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    return outputFile
                }
        }
    }


    fun createFileFromBitmap(bitmap: Bitmap, verification: String, imageSide: String): File {
        File(context.filesDir, generateFileName(verification, imageSide)).let { outputFile ->
            FileOutputStream(outputFile, false).use { outputStream ->
                outputStream.write(
                    bitmap.toJpgByteArray()
                )
            }
            return outputFile
        }
    }

    fun createMLModelFile(url: String): File {
        return File(context.cacheDir, generateMLModelFileName(url))
    }

    private fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.FaluIdentityFileProvider",
            file
        )

    internal val imageFile: File
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

    private fun generateMLModelFileName(url: String): String {
        val httpUrl = url.toHttpUrl()
        return httpUrl.encodedPathSegments[4]
    }

    private val imageFileName: String
        get() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}