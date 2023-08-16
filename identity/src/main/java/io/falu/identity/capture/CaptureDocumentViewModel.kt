package io.falu.identity.capture

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.identity.utils.FileUtils

internal class CaptureDocumentViewModel(
    private val stateHandle: SavedStateHandle
) : ViewModel() {

    private lateinit var imageCaptureFront: ImageCapture
    private lateinit var imageCaptureBack: ImageCapture
    private lateinit var frontImagePicker: ImagePicker
    private lateinit var backImagePicker: ImagePicker

    /**
     *
     */
    fun captureDocumentImages(
        caller: ActivityResultCaller,
        utils: FileUtils,
        onFrontImageCaptured: (Uri) -> Unit,
        onBackImageCaptured: (Uri) -> Unit
    ) {
        imageCaptureFront =
            ImageCapture(caller, utils, stateHandle, KEY_FRONT_IMAGE_URI, onFrontImageCaptured)
        imageCaptureBack =
            ImageCapture(caller, utils, stateHandle, KEY_BACK_IMAGE_URI, onBackImageCaptured)
    }

    /**
     *
     */
    fun pickDocumentImages(
        fragment: Fragment,
        onFrontImagePicked: (Uri) -> Unit,
        onBackImagePicked: (Uri) -> Unit
    ) {
        frontImagePicker = ImagePicker(fragment, onFrontImagePicked)
        backImagePicker = ImagePicker(fragment, onBackImagePicked)
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

    internal class CaptureDocumentViewModelFactory(
        ownerProvider: () -> SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(ownerProvider(), null) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return CaptureDocumentViewModel(handle) as T
        }
    }

    internal companion object {
        const val KEY_FRONT_IMAGE_URI = ":front"
        const val KEY_BACK_IMAGE_URI = ":back"
    }
}