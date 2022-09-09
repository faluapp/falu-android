package io.falu.identity

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

interface FaluIdentityVerificationView {

    /**
     * Start the identity verification process
     * @param
     */
    fun open(verificationId: String, temporaryKey: String)

    companion object {
        fun create(
            activity: ComponentActivity,
            logo: Uri,
            callback: IdentityVerificationResultCallback
        ): FaluIdentityVerificationView = IdentityVerificationView(activity, logo, callback)

        fun create(
            fragment: Fragment,
            logo: Uri,
            callback: IdentityVerificationResultCallback
        ): FaluIdentityVerificationView = IdentityVerificationView(fragment, logo, callback)
    }
}