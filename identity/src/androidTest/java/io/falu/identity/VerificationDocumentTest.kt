package io.falu.identity

import android.graphics.Rect
import android.util.Size
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import io.falu.identity.test.R
import io.falu.identity.utils.crop
import io.falu.identity.utils.maxAspectRatio
import io.falu.identity.utils.scale
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class VerificationDocumentTest {

    private val testResources = InstrumentationRegistry.getInstrumentation().context.resources

    @Test
    fun test_document_image_scales_correctly() {
        val bitmap = testResources.getDrawable(R.drawable.verification_document, null).toBitmap()

        assertNotNull(bitmap)

        // makes sure its a valid image
        // The height and width must be greater than 0
        assertNotEquals(0, bitmap.height, "Image height is 0")
        assertNotEquals(0, bitmap.width, "Image width is 0")

        val scaledImage = bitmap.scale(Size(bitmap.width / 2, bitmap.height / 2))

        assertEquals(
            Size(bitmap.width / 2, bitmap.height / 2),
            Size(scaledImage.width, scaledImage.height),
            "Wrong image size for the scaled bitmap"
        )
    }

    @Test
    fun test_cropping_works_correctly() {
        val bitmap = testResources.getDrawable(R.drawable.verification_document, null).toBitmap()

        assertNotNull(bitmap)

        // makes sure its a valid image
        // The height and width must be greater than 0
        assertNotEquals(0, bitmap.height, "Image height is 0")
        assertNotEquals(0, bitmap.width, "Image width is 0")

        val croppedBitmap = bitmap.crop(
            Rect(
                bitmap.width / 3,
                bitmap.height / 3,
                bitmap.width * 2 / 3,
                bitmap.height * 2 / 3
            )
        )

        assertEquals(
            Size(bitmap.width * 2 / 3 - bitmap.width / 3, bitmap.height * 2 / 3 - bitmap.height / 3),
            Size(croppedBitmap.width, croppedBitmap.height),
            "Cropped image is the wrong size"
        )
    }

    @Test
    fun test_max_aspect_ratio_same() {
        val size = Size(4, 3)
        val scaledSize = size.maxAspectRatio(4.toFloat() / 3)

        assertEquals(size, scaledSize)
    }

    @Test
    fun test_max_aspect_ratio_height_long() {
        val size = Size(4, 4)
        val scaledSize = size.maxAspectRatio(3.toFloat() / 4)

        assertEquals(Size(3, 4), scaledSize)
    }

    @Test
    fun test_max_aspect_ratio_width_long() {
        val size = Size(4, 3)
        val scaledSize = size.maxAspectRatio(4.toFloat() / 3)

        assertEquals(Size(4, 3), scaledSize)
    }
}