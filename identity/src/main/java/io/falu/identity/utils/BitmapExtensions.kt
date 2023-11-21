package io.falu.identity.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import android.util.Size
import androidx.annotation.CheckResult
import java.io.ByteArrayOutputStream

/**
 *
 */
@CheckResult
internal fun Bitmap.toJpgByteArray(quality: Int = 80): ByteArray =
    ByteArrayOutputStream().use {
        this.compress(Bitmap.CompressFormat.JPEG, quality, it)
        it.flush()
        it.toByteArray()
    }

/**
 *
 */
@CheckResult
internal fun Bitmap.scale(size: Size, filter: Boolean = false): Bitmap {
    if (size.height == height && size.width == width) {
        return this
    }
    return Bitmap.createScaledBitmap(this, size.width, size.height, filter)
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
internal fun Bitmap.rotate(angle: Int = 90, filter: Boolean = false): Bitmap {
    val matrix = Matrix().apply {
        postRotate(angle.toFloat())
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, filter)
}

/**
 * Create a [Bitmap] from [Rect] coordinates
 */
internal fun Bitmap.crop(rect: Rect): Bitmap {
    require(rect.left < rect.right && rect.top < rect.bottom) { "Cannot crop negative values" }
    require(
        rect.left >= 0 &&
                rect.top >= 0 &&
                rect.bottom <= this.height &&
                rect.right <= this.width
    ) {
        "Invalid dimensions for crop"
    }
    return Bitmap.createBitmap(this, rect.left, rect.top, rect.width(), rect.height())
}

/**
 * Get [Bitmap] [Size]
 */
@CheckResult
internal fun Bitmap.toSize() = Size(this.width, this.height)

/**
 *
 */
@CheckResult
internal fun Bitmap.withBoundingBox(bounds: Rect): Bitmap {
    val bitmap = copy(config, true)
    val canvas = Canvas(bitmap)

    Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
        canvas.drawRect(bounds, this)
    }

    return bitmap
}

/**
 * Crop a [Bitmap] at the from the center given the [Size]
 */
@CheckResult
internal fun Bitmap.centerCrop(size: Size): Bitmap {
    return crop(size.center(size.toRect()))
}

/**
 *
 */
@CheckResult
internal fun Uri.toBitmap(resolver: ContentResolver): Bitmap {
    return resolver.openInputStream(this).use {
        BitmapFactory.decodeStream(it)
    }
}