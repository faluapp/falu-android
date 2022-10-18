package io.falu.identity.utils

import androidx.fragment.app.Fragment
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.api.models.verification.VerificationUploadRequest
import software.tingle.api.patch.JsonPatchDocument

internal fun Fragment.updateVerification(
    viewModel: IdentityVerificationViewModel,
    document: JsonPatchDocument,
    onSuccess: (() -> Unit)
) {
    viewModel.updateVerification(document, onSuccess = { onSuccess() }, onFailure = {})
}

internal fun Fragment.submitVerificationData(
    viewModel: IdentityVerificationViewModel,
    verificationUploadRequest: VerificationUploadRequest
) {
    viewModel.submitVerificationData(
        verificationUploadRequest,
        onSuccess = {
            when {
                it.hasRequirementErrors -> {
                    // TODO: 2022-10-18
                }
                it.submitted -> {
                    // TODO: Show confirmation fragment
                }
                else -> {
                    // TODO: 2022-10-18 Open error page
                }
            }
        },
        onFailure = {}
    )
}