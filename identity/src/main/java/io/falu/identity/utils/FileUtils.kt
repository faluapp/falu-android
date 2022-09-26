package io.falu.identity.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

internal class FileUtils internal constructor(private val context: Context) {

    val imageUri: Uri
        get() {
            imageFile.also { return getFileUri(imageFile) }
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

    private val imageFileName: String
        get() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}