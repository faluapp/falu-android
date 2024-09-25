package io.falu.identity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.falu.identity.screens.WelcomeScreen

@Composable
internal fun IdentityNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    identityViewModel: IdentityVerificationViewModel,
    startDestination: String,
    navActions: IdentityVerificationNavActions = remember(navController) {
        IdentityVerificationNavActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(IdentityDestinations.WELCOME_ROUTE) {
            WelcomeScreen(viewModel = identityViewModel)
        }
    }
}