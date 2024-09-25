package io.falu.identity

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import io.falu.identity.IdentityDestinations.WELCOME_ROUTE

private object IdentityScreens {
    const val WELCOME = "welcome"
    const val SUPPORT = "support"
}

internal class IdentityVerificationNavActions(private val navController: NavController) {
    fun navigateToWelcome(viewModel: IdentityVerificationViewModel) {
        navController.navigate(WELCOME_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }
}

object IdentityDestinations {
    const val WELCOME_ROUTE = IdentityScreens.WELCOME
    const val SUPPORT_ROUTE = IdentityScreens.SUPPORT
}