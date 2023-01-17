package io.falu.identity.utils

import android.content.Context
import android.graphics.*
import android.media.Image
import android.text.TextUtils
import android.util.Size
import androidx.annotation.CheckResult
import androidx.annotation.RestrictTo
import androidx.camera.core.AspectRatio
import androidx.exifinterface.media.ExifInterface
import io.falu.identity.R
import software.tingle.api.HttpApiResponseProblem
import java.io.ByteArrayOutputStream
import java.io.File
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

/**
 *
 */
@CheckResult
internal fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

/**
 * Crop a given [Bitmap] given a the [Rect] size.
 *
 * The crop must be within the bounds of the [Bitmap]
 */
@CheckResult
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Bitmap.crop(rect: Rect): Bitmap {
    require(rect.left < rect.right && rect.top < rect.bottom) { "The rect size cannot be negative" }
    require(
        rect.left >= 0 &&
                rect.top >= 0 &&
                rect.bottom <= this.height &&
                rect.right <= this.width
    ) {
        "The defined size is greater than source image"
    }
    return Bitmap.createBitmap(this, rect.left, rect.top, rect.width(), rect.height())
}

@CheckResult
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Bitmap.toSize() = Size(this.width, this.height)

@CheckResult
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun Bitmap.centerCrop(size: Size): Bitmap {
    return if (size.width > width || size.height > height) {
        val region = size.centerScaleWithin(Size(width, height))
        crop(region)
    } else {
        val cake = toSize()
        crop(size.center(cake.toRect()))
    }
}

@CheckResult
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Size.center(rect: Rect) = Rect(
    rect.centerX() - this.width / 2, // left
    rect.centerY() - this.height / 2, // top
    rect.centerX() + this.width / 2, // right
    rect.centerY() + this.height / 2 // bottom
)

@CheckResult
fun Size.centerScaleWithin(bounds: Size): Rect {
    val aspectRatio = width.toFloat().div(height)

    val scaledSize = maxAspectRatioOf(bounds, aspectRatio)
    val left = (bounds.width - scaledSize.width) / 2
    val top = (bounds.height - scaledSize.height) / 2
    return Rect(
        left,
        top,
        left + scaledSize.width,
        top + scaledSize.height
    )
}

fun maxAspectRatioOf(area: Size, ratio: Float): Size {
    var w = area.width
    var h = (w / ratio).roundToInt()

    return if (h <= area.height) {
        Size(area.width, h)
    } else {
        h = area.height
        w = (h * ratio).roundToInt()
        Size(min(w, area.width), h)
    }
}

/**
 * Converts a size to rectangle with the top left corner at 0,0
 */
@CheckResult
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Size.toRect() = Rect(0, 0, this.width, this.height)

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
 *
 */
@CheckResult
internal fun Bitmap.rotate(angle: Int, filter: Boolean = false): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, filter)
}

/**
 *
 */
@CheckResult
internal fun Bitmap.adjustRotation(file: File): Bitmap {
    val exifInterface = ExifInterface(file)
    val orientation = exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate(90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate(180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate(270)
        ExifInterface.ORIENTATION_NORMAL -> this
        else -> rotate(90)
    }
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


internal fun Int.toFraction(): Float {
    return this.div(100F)
}

internal fun Float.toWholeNumber(): Int {
    return this.times(100F).toInt()
}