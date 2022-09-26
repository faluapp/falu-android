package io.falu.identity.capture

import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import io.falu.identity.utils.FileUtils

internal class CaptureDocumentViewModel() : ViewModel() {

    private lateinit var imageCaptureFront: ImageCapture
    private lateinit var imageCaptureBack: ImageCapture
    private lateinit var frontImagePicker: ImagePicker
    private lateinit var backImagePicker: ImagePicker

    /**
     *
     */
    fun captureDocumentImages(
        fragment: Fragment,
        fileUtils: FileUtils,
        onFrontImageCaptured: (Uri) -> Unit,
        onBackImageCaptured: (Uri) -> Unit
    ) {
        imageCaptureFront = ImageCapture(fragment, fileUtils, onFrontImageCaptured)
        imageCaptureBack = ImageCapture(fragment, fileUtils, onBackImageCaptured)
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
}