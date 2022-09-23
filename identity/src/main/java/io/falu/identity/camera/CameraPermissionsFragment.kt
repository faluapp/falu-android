package io.falu.identity.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.falu.identity.R

internal abstract class CameraPermissionsFragment : Fragment() {

    private lateinit var onCameraPermissionGranted: () -> Unit

    private lateinit var onCameraPermissionDenied: () -> Unit

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onCameraPermissionGranted()
            } else {
                onCameraPermissionDenied()
            }
        }

    private val permissionsAllowProceed: Boolean
        get() {
            if (!hasPermissions(requireContext())) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showCameraPermissionRationale()
                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                return false
            }
            return true
        }

    protected open fun requestCameraPermissions(
        onCameraPermissionGranted: (() -> Unit),
        onCameraPermissionDenied: (() -> Unit)
    ) {
        this.onCameraPermissionGranted = onCameraPermissionGranted
        this.onCameraPermissionDenied = onCameraPermissionDenied

        if (permissionsAllowProceed) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun showCameraPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_explanation_title)
            .setMessage(R.string.permission_explanation_camera)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // user has accepted our explanation so we can request the permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onCameraPermissionDenied
            }
            .show()
    }

    internal companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        /**
         * Convenience method used to check if all permissions required by this app are granted
         */
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}