package io.falu.identity

import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import io.falu.identity.navigation.InitialDestination
import io.falu.identity.navigation.WelcomeDestination

internal class IdentityBackPressHandler(
    private val navController: NavController,
    private val identityVerificationCallback: IdentityVerificationResultCallback
) : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (navController.previousBackStackEntry?.destination?.route == InitialDestination.ROUTE.route ||
            navController.previousBackStackEntry?.destination?.route == WelcomeDestination.ROUTE.route
        ) {
            finishWithCancelResult(identityVerificationCallback)
        }
    }

    private fun finishWithCancelResult(callback: IdentityVerificationResultCallback) {
        callback.onFinishWithResult(IdentityVerificationResult.Canceled)
    }
}