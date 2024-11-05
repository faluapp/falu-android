package io.falu.identity.navigation

import androidx.navigation.NavController
import io.falu.core.exceptions.ApiException
import io.falu.identity.viewModel.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.requirements.RequirementError
import io.falu.identity.api.models.requirements.RequirementType.Companion.matchesFromRoute
import io.falu.identity.utils.getErrorDescription

internal fun NavController.navigateWithRequirementErrors(
    route: String,
    requirementError: RequirementError,
) {
    navigateTo(
        ErrorDestination(
            title = requirementError.code,
            desc = requirementError.description,
            backButtonText = context.getString(R.string.button_rectify),
            requirementType = requirementError.requirement,
            backButtonDestination = if (requirementError.requirement!!.matchesFromRoute(route)) {
                route
            } else {
                ""
            }
        )
    )
}

internal fun NavController.navigateToErrorWithApiExceptions(throwable: Throwable?) {
    val error = (throwable as ApiException).problem

    navigateTo(
        ErrorDestination(
            title = context.getString(R.string.error_title),
            desc = error?.getErrorDescription(context) ?: context.getString(R.string.error_description_server),
            backButtonText = context.getString(R.string.button_cancel),
            throwable = throwable,
            cancelFlow = true
        )
    )
}

internal fun NavController.navigateToErrorWithFailure(throwable: Throwable?) {
    navigateTo(
        ErrorDestination(
            title = context.getString(R.string.error_title),
            desc = context.getString(R.string.error_title_unexpected_error),
            backButtonText = context.getString(R.string.button_cancel),
            throwable = throwable,
            cancelFlow = true
        )
    )
}

internal fun NavController.navigateWithDepletedAttempts() {
    navigateTo(
        ErrorDestination(
            title = context.getString(R.string.error_title_depleted_attempts),
            desc = context.getString(R.string.error_description_depleted_attempts),
            cancelFlow = true,
            throwable = Exception(context.getString(R.string.error_description_depleted_attempts)),
            backButtonText = context.getString(R.string.button_cancel)
        )
    )
}

private val DOCUMENT_UPLOAD_ROUTES = setOf(
    SelfieDestination.ROUTE.route,
    ScanCaptureDestination.ROUTE.route,
    ManualCaptureDestination.ROUTE.route,
    UploadCaptureDestination.ROUTE.route
)


internal fun NavController.resetAndNavigateUp(identityViewModel: IdentityVerificationViewModel): Boolean {
    currentBackStackEntry?.destination?.route?.let { currentEntryRoute ->
        if (DOCUMENT_UPLOAD_ROUTES.contains(currentEntryRoute)) {
            identityViewModel.resetDocumentUploadDisposition()
        }
    }

    previousBackStackEntry?.destination?.route?.let { previousEntryRoute ->
        if (DOCUMENT_UPLOAD_ROUTES.contains(previousEntryRoute)) {
            identityViewModel.resetDocumentUploadDisposition()
        }
    }

    return navigateUp()
}