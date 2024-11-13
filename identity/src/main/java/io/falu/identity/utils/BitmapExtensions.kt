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
import android.net.Uri
import android.renderscript.RenderScript
import android.util.Size
import androidx.annotation.CheckResult
import androidx.camera.core.ImageProxy
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
 * Convert ImageProxy to bitmap
 *
 * @param renderScript
 */
@CheckResult
internal fun ImageProxy.toBitmap(renderScript: RenderScript) = when (format) {
    ImageFormat.NV21 -> {
        NV21Utils.toBitmap(width, height, planes[0].buffer.toByteArray(), renderScript)
    }

    ImageFormat.YUV_420_888 -> {
        NV21Utils.toBitmap(
            width,
            height,
            NV21Utils.yuvPlanesToNV21(
                width,
                height,
                planes.toList().map { it.buffer }.toTypedArray(),
                planes.toList().map { it.rowStride }.toIntArray(),
                planes.toList().map { it.pixelStride }.toIntArray()
            ),
            renderScript
        )
    }

    else -> {
        throw Exception(format.toString())
    }
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
    val bitmap = copy(config!!    , true)
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