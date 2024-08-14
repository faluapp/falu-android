package io.falu.identity

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

internal class IdentityVerificationView private constructor(
    private val logo: Uri,
    private val maxNetworkRetries: Int = 0,
    private val launcher: ActivityResultLauncher<ContractArgs>
) : FaluIdentityVerificationView {

    constructor(
        activity: ComponentActivity,
        logoUri: Uri,
        maxNetworkRetries: Int = 0,
        callback: IdentityVerificationCallback
    ) : this(
        logoUri,
        maxNetworkRetries,
        activity.registerForActivityResult(
            IdentityVerificationViewContract(),
            callback::onVerificationResult
        )
    )

    constructor(
        fragment: Fragment,
        logoUri: Uri,
        maxNetworkRetries: Int = 0,
        callback: IdentityVerificationCallback
    ) : this(
        logoUri,
        maxNetworkRetries,
        fragment.registerForActivityResult(
            IdentityVerificationViewContract(),
            callback::onVerificationResult
        )
    )

    override fun open(verificationId: String, temporaryKey: String) {
        launcher.launch(
            ContractArgs(temporaryKey, verificationId, maxNetworkRetries, logo)
        )
    }
}