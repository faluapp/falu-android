package io.falu.identity.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.verification.VerificationUploadRequest
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.patch.JsonPatchDocument

internal fun Fragment.updateVerification(
    viewModel: IdentityVerificationViewModel,
    document: JsonPatchDocument,
    onSuccess: (() -> Unit)
) {
    viewModel.updateVerification(document,
        onSuccess = { onSuccess() },
        onError = {
            navigateToApiResponseProblemFragment(it)
        },
        onFailure = {
            navigateToErrorFragment()
        })
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
                    findNavController()
                        .navigate(R.id.action_global_fragment_confirmation)
                }
            }
        },
        onError = {
            navigateToApiResponseProblemFragment(it)
        },
        onFailure = {
            navigateToErrorFragment()
        }
    )
}

/**
 * The destination is [ErrorFragment]
 */
internal fun Fragment.navigateToErrorFragment() {
    findNavController().navigate(R.id.action_global_fragment_error)
}

/**
 * The destination is [ApiResponseProblemFragment]
 */
internal fun Fragment.navigateToApiResponseProblemFragment(it: HttpApiResponseProblem?) {
    findNavController().navigate(R.id.action_global_fragment_api_response_problem)
}