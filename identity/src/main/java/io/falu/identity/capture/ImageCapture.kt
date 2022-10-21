package io.falu.identity.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.SavedStateHandle
import io.falu.identity.utils.FileUtils


/**
 * Use camera to capture an image
 */
internal class ImageCapture(
    caller: ActivityResultCaller,
    utils: FileUtils,
    stateHandle: SavedStateHandle,
    uriId: String,
    onImageCaptured: ((Uri) -> Unit)
) {
    private val capturedImageUri: Uri =
        stateHandle.get<Uri>(uriId) ?: run {
            val newUri = utils.internalFileUri
            stateHandle[uriId] = newUri
            newUri
        }

    private val cameraLauncher: ActivityResultLauncher<Intent> = caller.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            onImageCaptured(capturedImageUri)
        }
    }

    internal fun captureImage(context: Context) {
        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { captureImageIntent ->
            // ensure that there's a camera activity to handle the intent
            captureImageIntent.resolveActivity(context.packageManager).also {
                // create the File where the photo should go
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)
            }
        })
    }
}