package io.falu.identity.camera

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.falu.identity.R
import io.falu.identity.utils.getActivity
import io.falu.identity.utils.size
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

internal class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleEventObserver {

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
    internal val ivCameraBorder: ImageView

    private var _cameraViewType: CameraViewType = CameraViewType.DEFAULT
    private var _lensFacing: Int = CameraSelector.LENS_FACING_BACK

    @DrawableRes
    private var border: Int = BORDERLESS
    private var brightness: Double = 0.0

    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

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
    private lateinit var lifecycleOwner: LifecycleOwner

    /**
     *
     */
    var cameraViewType: CameraViewType
        get() = _cameraViewType
        set(value) {
            _cameraViewType = value
        }

    /**
     *
     */
    val analyzers: MutableList<ImageAnalysis.Analyzer> = mutableListOf()

    private val displayInfo by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireNotNull(context.getActivity()).display
        } else {
            null
        }
            ?: @Suppress("Deprecation")
            requireNotNull(context.getActivity()).windowManager.defaultDisplay
    }

    private val displayRotation by lazy { displayInfo.rotation }
    private val displayMetrics by lazy { DisplayMetrics().also { displayInfo.getRealMetrics(it) } }
    private val displaySize by lazy {
        Size(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels
        )
    }

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

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> onDestroyed()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_ANY -> onAny()
        }
    }

    /**
     *
     */
    private fun configureDimensions() {
        listOf(viewCameraPreview, ivCameraBorder).forEach { view ->
            (view.layoutParams as LayoutParams).dimensionRatio = cameraViewType.ratio.first
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
        withCameraProvider {
            bindCameraUseCases(it)
        }
    }

    @Synchronized
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        if (preview == null) {
            preview = Preview.Builder()
                .setTargetRotation(displayRotation)
                .setTargetResolution(viewCameraPreview.size())
                .build()
                .also {
                    it.setSurfaceProvider(viewCameraPreview.surfaceProvider)
                }
        }
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(displayRotation)
            .setTargetResolution(MINIMUM_RESOLUTION.toResolution(displaySize))
            .setImageQueueDepth(1)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    LumaAnalyzer { luma ->
                        brightness = luma
                    }
                )

                analyzers.forEach { analyzer ->
                    it.setAnalyzer(cameraExecutor, analyzer)
                }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

            val surfaceProvider = viewCameraPreview.surfaceProvider

            preview?.setSurfaceProvider(surfaceProvider)
        } catch (e: Throwable) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    internal fun startAnalyzer() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        withCameraProvider {
            configureCamera()
        }
    }

    internal fun stopAnalyzer() {
        imageAnalysis?.clearAnalyzer()
    }

    private fun onDestroyed() {
        withCameraProvider {
            it.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    private fun onPause() {
    }

    private fun onCreate() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewCameraPreview.post {
            configureCamera()
        }
    }

    private fun onStart() {
    }

    private fun onResume() {
    }

    private fun onStop() {
    }

    private fun onAny() {
    }

    internal fun unbindFromLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(this)
        withCameraProvider { cameraProvider ->
            preview?.let { preview ->
                cameraProvider.unbind(preview)
            }
        }
        onPause()
    }

    internal fun bindLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
        this.lifecycleOwner = lifecycleOwner
    }

    /**
     * Run a task with the camera provider.
     */
    private fun withCameraProvider(
        executor: Executor = ContextCompat.getMainExecutor(context),
        task: (ProcessCameraProvider) -> Unit
    ) {
        cameraProviderFuture.addListener({ task(cameraProviderFuture.get()) }, executor)
    }

    private fun Size.toResolution(display: Size) = when {
        display.width >= display.height -> Size(
            max(width, height), // width
            min(width, height) // height
        )

        else -> Size(
            min(width, height), // width
            max(width, height) // height
        )
    }

    internal enum class CameraViewType(val ratio: Pair<String, Int>) {
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
        PASSPORT(ASPECT_RATIO_PASSPORT),

        /**
         *
         */
        FACE(ASPECT_RATIO_FACE)
    }

    internal companion object {
        private val TAG = CameraView::class.java.simpleName
        private val ASPECT_RATIO_ID_CARD = Pair("3:2", 3 / 2)
        private val ASPECT_RATIO_PASSPORT = Pair("3:2", 3 / 2)
        private val ASPECT_RATIO_FACE = Pair("2:2", 2 / 2)
        private val ASPECT_RATIO_DEFAULT = Pair("16:9", 16 / 9)
        private const val BORDERLESS = -1
        private val MINIMUM_RESOLUTION = Size(1440, 1080)
    }
}