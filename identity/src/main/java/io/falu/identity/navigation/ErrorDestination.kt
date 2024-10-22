package io.falu.identity.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.falu.identity.api.models.requirements.RequirementError
import software.tingle.api.HttpApiResponseProblem

internal data class ErrorDestination(
    val title: String,
    val desc: String = "",
    val message: String = "",
    val backButtonText: String,
    val throwable: Throwable? = null,
    val backButtonDestination: String = "",
    val cancelFlow: Boolean = false
) : IdentityDestination() {

    override val workflowRoute = ROUTE

    override val routeWithArgs: String = workflowRoute.withParameters(
        KEY_ERROR_TITLE to title,
        KEY_ERROR_DESCRIPTION to desc,
        KEY_ERROR_MESSAGE to message,
        KEY_BACK_BUTTON_DESTINATION to backButtonDestination,
        KEY_BACK_BUTTON_TEXT to backButtonText,
        KEY_CANCEL_FLOW to cancelFlow
    )

    internal companion object {
        const val ERROR = "error"
        internal const val KEY_ERROR_TITLE = "title"
        internal const val KEY_ERROR_DESCRIPTION = "description"
        internal const val KEY_ERROR_MESSAGE = "message"
        internal const val KEY_ERROR_CAUSE = "cause"
        internal const val KEY_BACK_BUTTON_DESTINATION = "button-destination"
        internal const val KEY_BACK_BUTTON_TEXT = "back-button-text"
        internal const val KEY_CANCEL_FLOW = "cancel-flow"

        fun errorTitle(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_TITLE)

        fun errorDescription(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_DESCRIPTION)

        fun errorMessage(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_MESSAGE)

        fun errorCause(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_CAUSE)

        fun backButtonDestination(entry: NavBackStackEntry) = entry.getString(KEY_BACK_BUTTON_DESTINATION)

        fun backButtonText(entry: NavBackStackEntry) = entry.getString(KEY_BACK_BUTTON_TEXT)

        fun cancelFlow(entry: NavBackStackEntry) = entry.getBoolean(KEY_CANCEL_FLOW)

        val ROUTE = object : WorkflowRoute() {
            override val base: String = ERROR
            override val arguments = listOf(
                navArgument(KEY_ERROR_TITLE) {
                    type = NavType.StringType
                },
                navArgument(KEY_ERROR_DESCRIPTION) {
                    type = NavType.StringType
                },
                navArgument(KEY_ERROR_MESSAGE) {
                    type = NavType.StringType
                },
                navArgument(KEY_BACK_BUTTON_DESTINATION) {
                    type = NavType.StringType
                },
                navArgument(KEY_BACK_BUTTON_TEXT) {
                    type = NavType.StringType
                },
                navArgument(KEY_CANCEL_FLOW) {
                    type = NavType.BoolType
                }
            )
        }

        fun withRequirementErrors(
            error: RequirementError,
            backButtonText: String,
            backButtonDestination: String
        ) = ErrorDestination(
            title = error.code,
            desc = error.description,
            backButtonText = backButtonText,
            backButtonDestination = backButtonDestination,
            cancelFlow = false
        )

        fun withApiExceptions(
            title: String,
            desc: String,
            error: HttpApiResponseProblem?,
            backButtonText: String,
            backButtonDestination: String
        ) = ErrorDestination(
            title = title,
            desc = desc,
            backButtonText = backButtonText,
            backButtonDestination = backButtonDestination,
            cancelFlow = true
        )

        fun withApiFailure(
            title: String,
            desc: String,
            throwable: Throwable?,
            backButtonText: String,
            backButtonDestination: String
        ) = ErrorDestination(
            title = title,
            desc = desc,
            backButtonText = backButtonText,
            backButtonDestination = backButtonDestination,
            throwable = throwable,
            cancelFlow = true
        )

        fun withDepletedAttempts(
            title: String,
            desc: String,
            backButtonText: String,
            backButtonDestination: String
        ) = ErrorDestination(
            title = title,
            desc = desc,
            backButtonText = backButtonText,
            backButtonDestination = backButtonDestination,
            cancelFlow = true
        )

        fun withCameraTimeout(
            title: String,
            desc: String,
            message: String = "",
            backButtonText: String,
            backButtonDestination: String
        ) = ErrorDestination(
            title = title,
            desc = desc,
            message = message,
            backButtonText = backButtonText,
            backButtonDestination = backButtonDestination
        )
    }
}
