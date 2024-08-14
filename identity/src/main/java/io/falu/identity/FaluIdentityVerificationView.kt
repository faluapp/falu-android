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
            maxNetworkRetries: Int = 0,
            callback: IdentityVerificationCallback
        ): FaluIdentityVerificationView = IdentityVerificationView(activity, logo, maxNetworkRetries, callback)

        fun create(
            fragment: Fragment,
            logo: Uri,
            maxNetworkRetries: Int = 0,
            callback: IdentityVerificationCallback
        ): FaluIdentityVerificationView = IdentityVerificationView(fragment, logo, maxNetworkRetries, callback)
    }
}