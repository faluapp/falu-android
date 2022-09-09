package io.falu.identity

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

internal class IdentityVerificationView private constructor(
    private val logo: Uri,
    private val launcher: ActivityResultLauncher<ContractArgs>
) : FaluIdentityVerificationView {

    constructor(
        activity: ComponentActivity,
        logoUri: Uri,
        resultCallback: IdentityVerificationResultCallback
    ) : this(
        logoUri,
        activity.registerForActivityResult(
            IdentityVerificationViewContract(),
            resultCallback::onVerificationResult
        )
    )

    constructor(
        fragment: Fragment,
        logoUri: Uri,
        resultCallback: IdentityVerificationResultCallback
    ) : this(
        logoUri,
        fragment.registerForActivityResult(
            IdentityVerificationViewContract(),
            resultCallback::onVerificationResult
        )
    )

    override fun open(verificationId: String, temporaryKey: String) {
        launcher.launch(
            ContractArgs(temporaryKey, verificationId, logo)
        )
    }
}