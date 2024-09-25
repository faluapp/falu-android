package io.falu.identity.utils

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils
import android.util.Size
import android.view.View
import androidx.annotation.CheckResult
import androidx.annotation.RestrictTo
import io.falu.identity.R
import io.falu.identity.ai.DocumentOption
import io.falu.identity.scan.ScanDisposition
import software.tingle.api.HttpApiResponseProblem
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.math.roundToInt

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

/**
 * Determine the size of a [View].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.size() = Size(width, height)

@CheckResult
internal fun Int.toFraction(): Float {
    return this.div(100F)
}

@CheckResult
internal fun Float.toWholeNumber(): Int {
    return this.times(100F).toInt()
}

/**
 * Determine points from the the center of a [Rect]
 */
@CheckResult
internal fun Size.center(rect: Rect) = Rect(
    rect.centerX() - this.width / 2, // left
    rect.centerY() - this.height / 2, // top
    rect.centerX() + this.width / 2, // right
    rect.centerY() + this.height / 2 // bottom
)

/**
 * Get a rectangle [Rect], with top let corner at (0,0)
 */
@CheckResult
internal fun Size.toRect() = Rect(0, 0, this.width, this.height)

/**
 * Calculate the max size of a rect with given aspect ratio that can fit a specified a area.
 */
@CheckResult
internal fun Size.maxAspectRatio(ratio: Float): Size {
    var w = width
    var h = (w / ratio).roundToInt()

    println("width: $w; height:$h")

    return if (h <= height) {
        return Size(width, height)
    } else {
        h = height
        w = (h * ratio).roundToInt()

        println("w: $w; h:$h")
        Size(min(w, width), h)
    }
}

/***/
internal fun DocumentOption.matches(type: ScanDisposition.DocumentScanType): Boolean {
    return this == DocumentOption.DL_BACK && type == ScanDisposition.DocumentScanType.DL_BACK ||
            this == DocumentOption.DL_FRONT && type == ScanDisposition.DocumentScanType.DL_FRONT ||
            this == DocumentOption.ID_BACK && type == ScanDisposition.DocumentScanType.ID_BACK ||
            this == DocumentOption.ID_FRONT && type == ScanDisposition.DocumentScanType.ID_FRONT ||
            this == DocumentOption.PASSPORT && type == ScanDisposition.DocumentScanType.PASSPORT
}

internal fun Uri.isHttp() = this.scheme!!.startsWith("http")