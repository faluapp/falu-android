package io.falu.identity.utils

import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Select image from the local gallery
 */
internal class ImagePicker(
    activityResultCaller: ActivityResultCaller,
    onImageSelected: ((Uri) -> Unit)
) {
    private val launcher =
        activityResultCaller.registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                onImageSelected(uri)
            }
        }

    fun pickImage() {
        launcher.launch("image/*")
    }
}