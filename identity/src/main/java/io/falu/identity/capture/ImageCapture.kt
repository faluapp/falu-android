package io.falu.identity.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import io.falu.identity.utils.FileUtils


/**
 * Use camera to capture an image
 */
internal class ImageCapture(
    fragment: Fragment,
    private val fileUtils: FileUtils,
    onImageCaptured: ((Uri) -> Unit)
) {

    private val cameraLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            onImageCaptured(fileUtils.imageUri)
        }
    }

    internal fun captureImage(context: Context) {
        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { captureImageIntent ->
            // ensure that there's a camera activity to handle the intent
            captureImageIntent.resolveActivity(context.packageManager).also {
                // create the File where the photo should go
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUtils.imageUri)
            }
        })
    }
}