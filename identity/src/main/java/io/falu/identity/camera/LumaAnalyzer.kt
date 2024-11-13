package io.falu.identity.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.utils.toByteArray
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit

/** Helper type alias used for analysis use case callbacks */
internal typealias LumaListener = (luma: Double) -> Unit

private val FRAME_THRESHOLD = TimeUnit.SECONDS.toMillis(1)

/**
 *
 */
internal class LumaAnalyzer(val listener: LumaListener) : ImageAnalysis.Analyzer {
    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private var lastAnalyzedTimestamp = 0L

    var framesPerSecond: Double = -1.0
        private set

    override fun analyze(image: ImageProxy) {
        // Keep track of frames analyzed
        frameTimestamps.push(System.currentTimeMillis())

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        framesPerSecond = 1.0 / (
            (frameTimestamps.peekFirst() - frameTimestamps.peekLast()) / frameTimestamps.size.toDouble()
            ) * 1000.0

        // Calculate the average luma no more often than every second
        if (frameTimestamps.first - lastAnalyzedTimestamp >= FRAME_THRESHOLD) {
            // Since format in ImageAnalysis is YUV, image.planes[0] contains the Y (luminance) plane
            val buffer = image.planes[0].buffer
            // Extract image data from callback object
            val data = buffer.toByteArray()
            // Convert the data into an array of pixel values
            val pixels = data.map { it.toInt() and 0xFF }
            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listener(luma)

            // Update timestamp of last analyzed frame
            lastAnalyzedTimestamp = frameTimestamps.first
        }

        image.close()
    }
}