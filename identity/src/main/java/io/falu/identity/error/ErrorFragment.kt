package io.falu.identity.error

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.R
import io.falu.identity.api.models.requirements.RequirementError
import io.falu.identity.api.models.requirements.RequirementError.Companion.canNavigateBackTo
import io.falu.identity.utils.getErrorDescription
import software.tingle.api.HttpApiResponseProblem

internal class ErrorFragment(identityViewModelFactory: ViewModelProvider.Factory) :
    AbstractErrorFragment(identityViewModelFactory) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = requireArguments().getString(KEY_ERROR_TITLE)
        val desc = requireArguments().getString(KEY_ERROR_DESCRIPTION)
        val cancel = requireArguments().getBoolean(KEY_CANCEL_FLOW, false)
        val back =
            requireArguments().getString(KEY_BACK_BUTTON_TEXT, getString(R.string.button_cancel))
        val throwable = requireArguments().getSerializable(KEY_ERROR_CAUSE) as Throwable

        binding.tvErrorTitle.text = title
        binding.tvErrorDescription.text = desc
        binding.tvErrorMessage.visibility = View.GONE
        binding.buttonErrorActionPrimary.text = back

        // If cancel is `true` then terminate the workflow with an Exception
        if (cancel) {
            binding.buttonErrorActionPrimary.setOnClickListener {
                callback.onFinishWithResult(IdentityVerificationResult.Failed(throwable))
            }
        } else {
            binding.buttonErrorActionPrimary.setOnClickListener {
                val destination = requireArguments().getInt(KEY_BACK_BUTTON_DESTINATION)
                if (destination == UNKNOWN_DESTINATION) {
                    findNavController().navigate(DESTINATION_WELCOME_FRAGMENT)
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    internal companion object {
        private const val KEY_ERROR_TITLE = ":error-title"
        private const val KEY_ERROR_DESCRIPTION = ":error-description"
        private const val KEY_BACK_BUTTON_DESTINATION = ":error-button-destination"
        private const val KEY_BACK_BUTTON_TEXT = ":error-back-button-text"
        private const val KEY_CANCEL_FLOW = ":error-cancel-flow"
        private const val KEY_ERROR_CAUSE = ":error-cause"

        private const val UNKNOWN_DESTINATION = -1
        private val DESTINATION_WELCOME_FRAGMENT =
            R.id.fragment_welcome

        internal fun NavController.navigateWithRequirementErrors(
            context: Context,
            @IdRes source: Int,
            error: RequirementError
        ) {
            val bundle = bundleOf(
                KEY_ERROR_TITLE to error.code,
                KEY_ERROR_DESCRIPTION to error.description,
                KEY_BACK_BUTTON_DESTINATION to if (error.canNavigateBackTo(source = source))
                    source else UNKNOWN_DESTINATION,
                KEY_BACK_BUTTON_TEXT to context.getString(R.string.button_rectify),
                KEY_CANCEL_FLOW to false,
                KEY_ERROR_CAUSE to Exception("Identity verification requirement error: ${error.description}")
            )
            navigate(R.id.action_global_fragment_error, bundle)
        }

        internal fun NavController.navigateWithApiExceptions(
            context: Context,
            error: HttpApiResponseProblem?
        ) {
            val desc = error?.getErrorDescription(context)
                ?: context.getString(R.string.error_description_server)
            val bundle = bundleOf(
                KEY_ERROR_TITLE to  context.getString(R.string.error_title),
                KEY_ERROR_DESCRIPTION to desc,
                KEY_CANCEL_FLOW to true,
                KEY_ERROR_CAUSE to Exception("Api Exception: $error")
            )
            navigate(R.id.action_global_fragment_error, bundle)
        }

        internal fun NavController.navigateWithFailure(context: Context, throwable: Throwable) {
            val bundle = bundleOf(
                KEY_ERROR_TITLE to context.getString(R.string.error_title),
                KEY_ERROR_DESCRIPTION to context.getString(R.string.error_title_unexpected_error),
                KEY_CANCEL_FLOW to true,
                KEY_ERROR_CAUSE to throwable
            )
            navigate(R.id.action_global_fragment_error, bundle)
        }

        internal fun NavController.navigateWithDepletedAttempts(context: Context) {
            val bundle = bundleOf(
                KEY_ERROR_TITLE to context.getString(R.string.error_title_depleted_attempts),
                KEY_ERROR_DESCRIPTION to context.getString(R.string.error_description_depleted_attempts),
                KEY_CANCEL_FLOW to true,
                KEY_ERROR_CAUSE to Exception(context.getString(R.string.error_description_depleted_attempts))
            )

            navigate(R.id.action_global_fragment_error, bundle)
        }
    }
}