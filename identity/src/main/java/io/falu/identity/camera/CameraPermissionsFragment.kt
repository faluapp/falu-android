package io.falu.identity.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

internal abstract class CameraPermissionsFragment : Fragment() {

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

            }
        }

    private val permissionsAllowProceed: Boolean
        get() {
            if (!hasPermissions(requireContext())) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {

                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                return false
            }
            return true
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