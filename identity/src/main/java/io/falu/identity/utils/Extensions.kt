package io.falu.identity.utils

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.annotation.CheckResult
import io.falu.identity.R
import software.tingle.api.HttpApiResponseProblem
import java.io.ByteArrayOutputStream

internal fun HttpApiResponseProblem.getErrorDescription(context: Context): String {
    if (errors.isNullOrEmpty()) {
        val desc = description ?: code
        return context.getString(R.string.error_description_http_error, desc)
    }

    var desc = ""
    for (errors in errors!!.values) {
        desc = TextUtils.join("\n", errors)
    }
    return context.getString(R.string.error_description_http_error, desc)
}

@CheckResult
internal fun Bitmap.toJpgByteArray(quality: Int = 80): ByteArray =
    ByteArrayOutputStream().use {
        this.compress(Bitmap.CompressFormat.JPEG, quality, it)
        it.flush()
        it.toByteArray()
    }

internal fun Bitmap.restrictToSize(maxHeight: Int, maxWidth: Int, filter: Boolean = false): Bitmap {
    if (maxHeight >= height && maxWidth >= width) {
        // TODO: 26-10-2022 Calculate new sizes
        return Bitmap.createScaledBitmap(this, maxWidth, maxHeight, filter)
    }
    return this
}