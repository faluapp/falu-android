package io.falu.identity.utils

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.SavedStateHandle
import io.falu.identity.capture.CaptureDocumentViewModel
import io.falu.identity.capture.ImageCapture
import io.falu.identity.capture.ImagePicker

internal class IdentityImageHandler {

    private lateinit var imageCaptureFront: ImageCapture
    private lateinit var imageCaptureBack: ImageCapture
    private lateinit var frontImagePicker: ImagePicker
    private lateinit var backImagePicker: ImagePicker

    /**
     *
     */
    fun registerActivityResultCaller(
        caller: ActivityResultCaller,
        savedStateHandle: SavedStateHandle,
        utils: FileUtils,
        onFrontImageCaptured: (Uri) -> Unit,
        onBackImageCaptured: (Uri) -> Unit,
        onFrontImagePicked: (Uri) -> Unit,
        onBackImagePicked: (Uri) -> Unit
    ) {
        imageCaptureFront =
            ImageCapture(
                caller,
                utils,
                savedStateHandle,
                CaptureDocumentViewModel.KEY_FRONT_IMAGE_URI,
                onFrontImageCaptured
            )
        imageCaptureBack =
            ImageCapture(
                caller,
                utils,
                savedStateHandle,
                CaptureDocumentViewModel.KEY_BACK_IMAGE_URI,
                onBackImageCaptured
            )
        frontImagePicker = ImagePicker(caller, onFrontImagePicked)
        backImagePicker = ImagePicker(caller, onBackImagePicked)
    }

    /**
     *
     */
    fun captureImageFront(context: Context) {
        imageCaptureFront.captureImage(context)
    }

    /**
     *
     */
    fun captureImageBack(context: Context) {
        imageCaptureBack.captureImage(context)
    }

    /**
     * Pick an image for front.
     */
    fun pickImageFront() {
        frontImagePicker.pickImage()
    }

    /**
     * Pick an image for back.
     */
    fun pickImageBack() {
        backImagePicker.pickImage()
    }

    internal companion object {
        const val KEY_FRONT_IMAGE_URI = ":front"
        const val KEY_BACK_IMAGE_URI = ":back"
    }
}