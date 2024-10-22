package io.falu.identity.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

internal abstract class IdentityDestination(val popUpToParam: PopUpTo? = null) {

    internal abstract class WorkflowRoute {
        abstract val base: String
        open val arguments: List<NamedNavArgument> = emptyList()

        /**
         * Navigation route for the screen, built using the base route and arguments:
         * base?arg1={arg1}&arg2={arg2}
         */
        val route: String
            get() {
                val argumentsString = arguments
                    .mapIndexed { index, argument ->
                        val separator = if (index == 0) "?" else "&"
                        "$separator${argument.name}={${argument.name}}"
                    }
                    .joinToString("")

                return "$base$argumentsString"
            }
    }

    abstract val workflowRoute: WorkflowRoute

    open val routeWithArgs: String
        get() = workflowRoute.route
}

internal fun IdentityDestination.WorkflowRoute.withParameters(vararg parameters: Pair<String, Any?>): String {
    var route = this.route
    parameters.forEach { (key, value) ->
        route = route.replace("{$key}", value.toString())
    }
    return route
}

internal data class PopUpTo(
    val route: String,
    val inclusive: Boolean
)

internal fun NavController.navigateTo(destination: IdentityDestination) {
    navigate(destination.routeWithArgs) {
        destination.popUpToParam?.let {
            popUpTo(it.route) {
                inclusive = it.inclusive
            }
        }
    }
}

internal fun NavBackStackEntry?.getString(arg: String) = this?.arguments?.getString(arg)

internal fun NavBackStackEntry?.getInt(arg: String) =
    this?.arguments?.getInt(arg, 0) ?: 0

internal fun NavBackStackEntry?.getBoolean(arg: String) =
    this?.arguments?.getBoolean(arg, false) ?: false