package io.falu.identity.capture

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

class CaptureDocumentViewModel() : ViewModel() {

    private lateinit var frontImagePicker: ImagePicker
    private lateinit var backImagePicker: ImagePicker

    /**
     *
     */
    fun captureDocumentImages(
        fragment: Fragment,
        onFrontImagePicked: (Uri) -> Unit,
        onBackImagePicked: (Uri) -> Unit
    ) {
        frontImagePicker = ImagePicker(fragment, onFrontImagePicked)
        backImagePicker = ImagePicker(fragment, onBackImagePicked)
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