package io.falu.identity.utils

import androidx.fragment.app.Fragment
import io.falu.identity.IdentityVerificationViewModel
import software.tingle.api.patch.JsonPatchDocument

internal fun Fragment.updateVerification(
    viewModel: IdentityVerificationViewModel,
    document: JsonPatchDocument,
    onSuccess: (() -> Unit)
) {
    viewModel.updateVerification(document, onSuccess = { onSuccess() }, onFailure = {})
}