@file:Suppress("deprecation") // ktlint-disable annotation
package io.falu.identity.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import androidx.annotation.CheckResult
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import kotlin.experimental.inv

object NV21Utils {
    /**
     *  Function for converting YUV planes to NV21 byte array
     *
     *  REF: https://stackoverflow.com/questions/52726002/camera2-captured-picture-conversion-from-yuv-420-888-to-nv21/52740776#52740776
     */
    @CheckResult
    internal fun yuvPlanesToNV21(
        width: Int,
        height: Int,
        planeBuffers: Array<ByteBuffer>,
        rowStrides: IntArray,
        pixelStrides: IntArray
    ): ByteArray {
        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)
        val yBuffer = planeBuffers[0] // Y
        val uBuffer = planeBuffers[1] // U
        val vBuffer = planeBuffers[2] // V
        var rowStride = rowStrides[0]
        check(pixelStrides[0] == 1)
        var pos = 0
        if (rowStride == width) { // likely
            yBuffer[nv21, 0, ySize]
            pos += ySize
        } else {
            var yBufferPos = -rowStride.toLong() // not an actual position
            while (pos < ySize) {
                yBufferPos += rowStride.toLong()
                yBuffer.position(yBufferPos.toInt())
                yBuffer[nv21, pos, width]
                pos += width
            }
        }
        rowStride = rowStrides[2]
        val pixelStride = pixelStrides[2]
        check(rowStride == rowStrides[1])
        check(pixelStride == pixelStrides[1])
        if (pixelStride == 2 && rowStride == width && uBuffer[0] == vBuffer[1]) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            val savePixel = vBuffer[1]
            try {
                vBuffer.put(1, savePixel.inv())
                if (uBuffer[0] == savePixel.inv()) {
                    vBuffer.put(1, savePixel)
                    vBuffer.position(0)
                    uBuffer.position(0)
                    vBuffer[nv21, ySize, 1]
                    uBuffer[nv21, ySize + 1, uBuffer.remaining()]
                    return nv21 // shortcut
                }
            } catch (ex: ReadOnlyBufferException) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel)
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer[vuPos]
                nv21[pos++] = uBuffer[vuPos]
            }
        }

        return nv21
    }

    /**
     *
     */
    @CheckResult
    internal fun toBitmap(width: Int, height: Int, nv21Data: ByteArray, renderScript: RenderScript): Bitmap {
        val yuvTypeBuilder: Type.Builder =
            Type.Builder(renderScript, Element.U8(renderScript)).setX(nv21Data.size)
        val yuvType: Type = yuvTypeBuilder.create()
        val yuvAllocation = Allocation.createTyped(renderScript, yuvType, Allocation.USAGE_SCRIPT)
        yuvAllocation.copyFrom(nv21Data)

        val rgbTypeBuilder: Type.Builder =
            Type.Builder(renderScript, Element.RGBA_8888(renderScript))
        rgbTypeBuilder.setX(width)
        rgbTypeBuilder.setY(height)
        val rgbAllocation = Allocation.createTyped(renderScript, rgbTypeBuilder.create())

        val yuvToRgbScript =
            ScriptIntrinsicYuvToRGB.create(renderScript, Element.RGBA_8888(renderScript))
        yuvToRgbScript.setInput(yuvAllocation)
        yuvToRgbScript.forEach(rgbAllocation)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        rgbAllocation.copyTo(bitmap)

        // remove allocated objects
        yuvType.destroy()
        yuvAllocation.destroy()
        rgbAllocation.destroy()
        yuvToRgbScript.destroy()

        return bitmap
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
}