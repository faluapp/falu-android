@file:Suppress("deprecation") // ktlint-disable annotation
package io.falu.identity.utils

import android.content.Context
import android.renderscript.RenderScript
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.falu.core.exceptions.ApiException
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.capture.scan.DocumentScanViewModel
import io.falu.identity.error.ErrorFragment.Companion.navigateWithApiExceptions
import io.falu.identity.error.ErrorFragment.Companion.navigateWithFailure
import io.falu.identity.error.ErrorFragment.Companion.navigateWithRequirementErrors
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.patch.JsonPatchDocument
import java.io.File

internal fun Fragment.updateVerification(
    viewModel: IdentityVerificationViewModel,
    document: JsonPatchDocument,
    @IdRes source: Int,
    onSuccess: (() -> Unit)
) {
    viewModel.updateVerification(document,
        onSuccess = {
            when {
                else -> {
                    onSuccess()
                }
            }
        },
        onError = {
            navigateToApiResponseProblemFragment((it as ApiException).problem)
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
                    findNavController().navigate(R.id.action_global_fragment_confirmation)
                }
            }
        },
        onError = {
            navigateToApiResponseProblemFragment((it as ApiException).problem)
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

internal fun Fragment.loadDocumentDetectionModel(
    identityViewModel: IdentityVerificationViewModel,
    documentScanViewModel: DocumentScanViewModel,
    threshold: Float,
    onLoad: (File) -> Unit
) {
    identityViewModel.documentDetectorModelFile.observe(viewLifecycleOwner) {
        if (it != null) {
            documentScanViewModel.initialize(it, threshold)
            onLoad(it)
        }
    }
}

/***/
internal fun Fragment.showDatePickerDialog(onPositiveClickListener: (Long) -> Unit) {
    val datePicker = MaterialDatePicker.Builder
        .datePicker()
        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        .build()
    datePicker.addOnPositiveButtonClickListener {
        onPositiveClickListener(it)
    }
    datePicker.show(childFragmentManager, "MaterialDatePicker")
}

/***/
internal fun Context.showDialog(
    title: String? = null,
    message: String? = null,
    positiveButton: Pair<String, () -> Unit>,
    negativeButton: Pair<String, () -> Unit>? = null
) {
    val dialog = MaterialAlertDialogBuilder(this).setMessage(message)
        .setTitle(title)
        .setPositiveButton(positiveButton.first) { _, _ ->
            positiveButton.second()
        }

    if (negativeButton != null) {
        dialog.setNegativeButton(negativeButton.first) { _, _ ->
            negativeButton.second
        }
    }

    dialog.show()
}

/***/
internal fun Context.getRenderScript() = RenderScript.create(this)