package io.falu.identity.capture

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * Select image from the local gallery
 */
internal class ImagePicker(
    fragment: Fragment,
    onImageSelected: ((Uri) -> Unit)
) {
    private val launcher =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                onImageSelected(uri)
            }
        }

    fun pickImage() {
        launcher.launch("image/*")
    }
}