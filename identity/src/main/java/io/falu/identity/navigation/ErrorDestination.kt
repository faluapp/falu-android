package io.falu.identity.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.falu.identity.api.models.requirements.RequirementType

internal data class ErrorDestination(
    val title: String,
    val desc: String = KEY_UNSET,
    val message: String = KEY_UNSET,
    val throwable: Throwable? = null,
    val requirementType: RequirementType? = null,
    val primaryButtonText: String = KEY_UNSET,
    val backButtonText: String,
    val backButtonDestination: String = KEY_UNSET,
    val cancelFlow: Boolean = false
) : IdentityDestination() {

    override val workflowRoute = ROUTE

    override val routeWithArgs: String = workflowRoute.withParameters(
        KEY_ERROR_TITLE to title,
        KEY_ERROR_DESCRIPTION to desc,
        KEY_ERROR_MESSAGE to message,
        KEY_BACK_BUTTON_DESTINATION to backButtonDestination,
        KEY_BACK_BUTTON_TEXT to backButtonText,
        KEY_CANCEL_FLOW to cancelFlow,
        KEY_PRIMARY_BUTTON_TEXT to primaryButtonText,
        KEY_REQUIREMENT_TYPE to requirementType
    )

    internal companion object {
        const val ERROR = "error"
        internal const val KEY_ERROR_TITLE = "title"
        internal const val KEY_ERROR_DESCRIPTION = "description"
        internal const val KEY_ERROR_MESSAGE = "message"
        internal const val KEY_ERROR_CAUSE = "cause"
        internal const val KEY_BACK_BUTTON_DESTINATION = "buttonDestination"
        internal const val KEY_BACK_BUTTON_TEXT = "backButtonText"
        internal const val KEY_CANCEL_FLOW = "cancelFlow"
        internal const val KEY_PRIMARY_BUTTON_TEXT = "primaryButtonText"
        internal const val KEY_REQUIREMENT_TYPE = "requirementType"
        internal const val KEY_UNSET = "unset"

        fun errorTitle(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_TITLE)

        fun errorDescription(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_DESCRIPTION)

        fun errorMessage(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_MESSAGE).let {
            if (it == KEY_UNSET) {
                null
            } else {
                it
            }
        }

        fun errorCause(entry: NavBackStackEntry) = entry.getString(KEY_ERROR_CAUSE)

        fun backButtonDestination(entry: NavBackStackEntry) = entry.getString(KEY_BACK_BUTTON_DESTINATION).let {
            if (it == KEY_UNSET) {
                null
            } else {
                it
            }
        }

        fun backButtonText(entry: NavBackStackEntry) = entry.getString(KEY_BACK_BUTTON_TEXT).let {
            if (it == KEY_UNSET) {
                null
            } else {
                it
            }
        }

        fun cancelFlow(entry: NavBackStackEntry) = entry.getBoolean(KEY_CANCEL_FLOW)

        fun primaryButtonOptions(backStackEntry: NavBackStackEntry): Pair<String, RequirementType>? {
            val primaryButtonText = backStackEntry.getString(KEY_PRIMARY_BUTTON_TEXT)
            val primaryButtonRequirementType: RequirementType? =
                backStackEntry.getString(KEY_REQUIREMENT_TYPE)
                    .let { requirementString ->
                        if (requirementString.isNullOrEmpty()) {
                            null
                        } else {
                            RequirementType.valueOf(requirementString)
                        }
                    }

            return if (!primaryButtonText.isNullOrEmpty() && primaryButtonRequirementType != null) {
                (primaryButtonText to primaryButtonRequirementType)
            } else {
                null
            }
        }

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
    }
}