package io.falu.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.annotation.RestrictTo
import io.falu.core.exceptions.ApiException
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import software.tingle.api.ResourceResponse
import java.io.File

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun File.getMediaType(context: Context): MediaType {
    val uri = Uri.fromFile(this)

    val mimeType: String? = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val resolver = context.contentResolver
        resolver.getType(uri)
    } else {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }

    return if (!mimeType.isNullOrEmpty()) {
        mimeType.toMediaType()
    } else {
        "*/*".toMediaType()
    }
}

fun <TResult> ResourceResponse<TResult>.toThrowable() = ApiException(problem = error, statusCode = statusCode)