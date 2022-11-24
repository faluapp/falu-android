package io.falu.identity.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.error.ErrorFragment.Companion.navigateWithApiExceptions
import io.falu.identity.error.ErrorFragment.Companion.navigateWithFailure
import io.falu.identity.error.ErrorFragment.Companion.navigateWithRequirementErrors
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.patch.JsonPatchDocument

internal fun Fragment.updateVerification(
    viewModel: IdentityVerificationViewModel,
    document: JsonPatchDocument,
    @IdRes source: Int,
    onSuccess: (() -> Unit)
) {
    viewModel.updateVerification(document,
        onSuccess = {
            when {
                it.hasRequirementErrors -> {
                    findNavController().navigateWithRequirementErrors(
                        requireContext(),
                        source,
                        it.requirements.errors.first()
                    )
                }
                else -> {
                    onSuccess()
                }
            }
        },
        onError = {
            navigateToApiResponseProblemFragment(it)
        },
        onFailure = {
            navigateToErrorFragment(it)
        })
}

internal fun Fragment.submitVerificationData(
    viewModel: IdentityVerificationViewModel,
    @IdRes source: Int,
    verificationUploadRequest: VerificationUploadRequest
) {
    viewModel.submitVerificationData(
        verificationUploadRequest,
        onSuccess = {
            when {
                it.hasRequirementErrors -> {
                    findNavController().navigateWithRequirementErrors(
                        requireContext(),
                        source,
                        it.requirements.errors.first()
                    )
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
    findNavController().navigateWithFailure(requireContext(), it)
}

/**
 *
 */
internal fun Fragment.navigateToApiResponseProblemFragment(it: HttpApiResponseProblem?) {
    findNavController().navigateWithApiExceptions(requireContext(), it)
}

/**
 *
 */
fun <TResult> Fragment.getNavigationResult(key: String) =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<TResult>(key)

/***
 *
 */
fun <TResult> Fragment.setNavigationResult(key: String, result: TResult) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}