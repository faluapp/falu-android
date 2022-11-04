package io.falu.identity.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.LifecycleOwner
import io.falu.identity.R
import io.falu.identity.api.models.CameraLens
import io.falu.identity.api.models.CameraSettings
import io.falu.identity.api.models.Exposure
import io.falu.identity.utils.FileUtils

internal class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     *
     */
    private val viewCameraFrame: FrameLayout


    /**
     *
     */
    private val viewCameraPreview: PreviewView

    /**
     *
     */
    private val ivCameraBorder: ImageView

    /**
     *
     */
    private val fileUtils = FileUtils(context)

    private lateinit var _lifecycleOwner: LifecycleOwner

    private var _cameraViewType: CameraViewType = CameraViewType.DEFAULT
    private var _lensFacing: Int = CameraSelector.LENS_FACING_BACK

    @DrawableRes
    private var border: Int = BORDERLESS

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * Detects, characterizes, and connects to a CameraDevice (used for all camera operations)
     */
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

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

    var cameraViewType: CameraViewType
        get() = _cameraViewType
        set(value) {
            _cameraViewType = value
        }

    /**
     *
     */
    val cameraInfo: CameraInfo?
        get() = camera?.cameraInfo

    init {
        context.withStyledAttributes(attrs, R.styleable.CameraView) {
            if (attrs != null) {
                border = getResourceId(R.styleable.CameraView_cameraBorder, BORDERLESS)
            }
        }

        val view = inflate(context, R.layout.view_camera_preview, this)
        viewCameraPreview = view.findViewById(R.id.view_preview)
        viewCameraFrame = view.findViewById(R.id.view_camera_frame)
        ivCameraBorder = view.findViewById(R.id.iv_camera_border)

        post {
            configureDimensions()
            setBorder()
        }

        viewCameraPreview.post {
            configureCamera()
        }
    }

    /**
     *
     */
    private fun configureDimensions() {
        listOf(viewCameraPreview, ivCameraBorder).forEach { view ->
            (view.layoutParams as LayoutParams).dimensionRatio = cameraViewType.ratio
        }
    }

    /**
     *
     */
    private fun setBorder() {
        if (border != BORDERLESS) {
            // the space between the Camera Preview and other view areas
            ivCameraBorder.background = ContextCompat.getDrawable(context, border)
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

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            val surfaceProvider = viewCameraPreview.surfaceProvider

            preview?.setSurfaceProvider(surfaceProvider)

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

    /**
     *
     */
    internal val cameraSettings: CameraSettings
        get() {
            val extensions = cameraManager.getCameraCharacteristics(cameraId)
            val focalLength =
                extensions.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.first()
            val duration =
                extensions.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)!!.lower
            val iso = extensions.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!.upper

            return CameraSettings(
                lens = CameraLens(model = "Camera-X", focalLength = focalLength!!),
                brightness = 0F,
                exposure = Exposure(iso = iso.toFloat(), duration = duration.toFloat())
            )
        }

    private val cameraId: String
        get() = cameraManager.cameraIdList.first()

    internal enum class CameraViewType(val ratio: String) {
        /**
         *
         */
        DEFAULT(ASPECT_RATIO_DEFAULT),

        /**
         *
         */
        ID(ASPECT_RATIO_ID_CARD),

        /**
         *
         */
        PASSPORT(ASPECT_RATIO_PASSPORT)
    }

    internal companion object {
        private val TAG = CameraView::class.java.simpleName
        private const val ASPECT_RATIO_ID_CARD = "3:2"
        private const val ASPECT_RATIO_PASSPORT = "3:2"
        private const val ASPECT_RATIO_DEFAULT = "2:2"
        private const val BORDERLESS = -1
    }
}