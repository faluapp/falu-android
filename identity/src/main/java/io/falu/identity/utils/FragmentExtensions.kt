package io.falu.identity.utils

import androidx.core.os.bundleOf
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
            navigateToErrorFragment(it)
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
            navigateToErrorFragment(it)
        }
    )
}

/**
 * The destination is [ErrorFragment]
 */
internal fun Fragment.navigateToErrorFragment(it: Throwable) {
    val bundle = bundleOf(KEY_ERROR_CAUSE to it)
    findNavController().navigate(R.id.action_global_fragment_error, bundle)
}

/**
 * The destination is [ApiResponseProblemFragment]
 */
internal fun Fragment.navigateToApiResponseProblemFragment(it: HttpApiResponseProblem?) {
    findNavController().navigate(R.id.action_global_fragment_api_response_problem)
}

internal const val KEY_ERROR_CAUSE = "cause"