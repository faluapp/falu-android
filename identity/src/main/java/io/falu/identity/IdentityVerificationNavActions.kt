package io.falu.identity

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import io.falu.identity.IdentityDestinations.DOCUMENT_SELECTION_ROUTE
import io.falu.identity.IdentityDestinations.WELCOME_ROUTE

private object IdentityScreens {
    const val WELCOME = "welcome"
    const val SUPPORT = "support"
    const val DOCUMENT_SELECTION = "document_selection"
}

internal class IdentityVerificationNavActions(private val navController: NavController) {
    fun navigateToWelcome() {
        navController.navigate(WELCOME_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToDocumentSelection() {
        navController.navigate(DOCUMENT_SELECTION_ROUTE) {
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
    const val DOCUMENT_SELECTION_ROUTE = IdentityScreens.DOCUMENT_SELECTION
    const val SUPPORT_ROUTE = IdentityScreens.SUPPORT
}