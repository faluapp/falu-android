package io.falu.identity.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.media.Image
import android.text.TextUtils
import androidx.annotation.CheckResult
import io.falu.identity.R
import software.tingle.api.HttpApiResponseProblem
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

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

@CheckResult
internal fun Image.toBitmap(): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}

/**
 *
 */
@CheckResult
internal fun Image.toJpegBitmap(): Bitmap {
    require(format == ImageFormat.JPEG) { "Image is not in JPEG format" }

    val buffer = planes[0].buffer
    val bytes = buffer.toByteArray()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

/**
 * Helper extension function used to extract a byte array from an
 * image plane buffer
 */
@CheckResult
internal fun ByteBuffer.toByteArray(): ByteArray {
    rewind() // Rewind the buffer to zero
    val data = ByteArray(remaining())
    get(data) // Copy the buffer into a byte array
    return data // Return the byte array
}