package io.falu.identity.camera

import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.falu.identity.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * TODO: document your custom view class.
 */
class CameraView : FrameLayout {
    private lateinit var windowManager: WindowManager
    private lateinit var viewCameraPreview: PreviewView
    private lateinit var _lifecycleOwner: LifecycleOwner

    private var preview: Preview? = null
    private var _lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * The direction of the camera, front or back
     */
    var lensFacing: Int
        get() = _lensFacing
        set(value) {
            _lensFacing = value
        }

    var lifecycleOwner: LifecycleOwner
        get() = _lifecycleOwner
        set(value) {
            _lifecycleOwner = value
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val view = inflate(context, R.layout.view_camera_preview, this)

        viewCameraPreview = view.findViewById(R.id.view_preview)
        viewCameraPreview.post {
            configureCamera()
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun configureCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Declare and bind preview, capture and analysis use cases
     */
    private fun bindCameraUseCases() {

        val rotation = viewCameraPreview.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            //   .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            // .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            // .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
//                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
//                    // Values returned from our analyzer are passed to the attached listener
//                    // We log image analysis results here - you should do something useful
//                    // instead!
//                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewCameraPreview.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            // Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
    }

    /**
     * Take photo using the camera
     */
    fun takePhoto() {

    }

    internal companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}