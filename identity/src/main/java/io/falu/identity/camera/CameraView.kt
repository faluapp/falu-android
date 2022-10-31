package io.falu.identity.camera

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.falu.identity.R
import io.falu.identity.utils.FileUtils

class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val fileUtils = FileUtils(context)
    private lateinit var _lifecycleOwner: LifecycleOwner

    private var viewCameraPreview: PreviewView
    private var preview: Preview? = null
    private var _lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
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

    /**
     * The lifecycle owner
     */
    var lifecycleOwner: LifecycleOwner
        get() = _lifecycleOwner
        set(value) {
            _lifecycleOwner = value
        }

    init {
        val view = inflate(context, R.layout.view_camera_preview, this)

        viewCameraPreview = view.findViewById(R.id.view_preview)
        viewCameraPreview.post {
            configureCamera()
        }
    }

    /**
     *
     */
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
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            preview?.setSurfaceProvider(viewCameraPreview.surfaceProvider)

            observeCameraState(camera?.cameraInfo!!)
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    /**
     *
     */
    private fun observeCameraState(cameraInfo: CameraInfo) {
        // TODO: 2022-10-31 Observe camera states
    }

    /**
     * Take photo using the camera
     */
    fun takePhoto(
        onCaptured: ((Uri?) -> Unit),
        onCaptureError: ((ImageCaptureException) -> Unit)
    ) {
        val imageCapture = imageCapture ?: return

        val outputOptions = ImageCapture
            .OutputFileOptions
            .Builder(fileUtils.imageFile)
            .build()

        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onCaptured(outputFileResults.savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onCaptureError(exception)
                }
            })
    }

    internal companion object {
        private val TAG = CameraView::class.java.simpleName
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}