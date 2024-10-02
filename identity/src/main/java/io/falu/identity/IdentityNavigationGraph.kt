package io.falu.identity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.screens.DocumentCaptureMethodsScreen
import io.falu.identity.screens.DocumentSelectionScreen
import io.falu.identity.screens.capture.UploadCaptureScreen
import io.falu.identity.screens.WelcomeScreen
import io.falu.identity.screens.capture.ManualCaptureScreen

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
            WelcomeScreen(
                viewModel = identityViewModel,
                navigateToDocumentSelection = { navActions.navigateToDocumentSelection() },
                navigateToError = {})
        }

        composable(IdentityDestinations.DOCUMENT_SELECTION_ROUTE) {
            DocumentSelectionScreen(
                viewModel = identityViewModel,
                navigateToCaptureMethods = { navActions.navigateToDocumentCaptureMethods(it) },
                navigateToError = {})
        }

        composable(
            IdentityDestinations.DOCUMENT_CAPTURE_METHODS_ROUTE,
            arguments = listOf(navArgument("documentType") {
                type = NavType.EnumType(IdentityDocumentType::class.java)
            })
        ) { entry ->
            val identityDocumentType = entry.arguments?.getSerializable("documentType") as IdentityDocumentType
            DocumentCaptureMethodsScreen(identityViewModel, identityDocumentType, navigateToCaptureMethod = {
                when (it) {
                    UploadMethod.AUTO -> navActions.navigateToScanCapture(documentType = identityDocumentType)
                    UploadMethod.MANUAL -> navActions.navigateToManualCapture(documentType = identityDocumentType)
                    UploadMethod.UPLOAD -> navActions.navigateToUploadCapture(documentType = identityDocumentType)
                }
            })
        }

        composable(
            IdentityDestinations.DOCUMENT_CAPTURE_METHOD_UPLOAD_ROUTE,
            arguments = listOf(navArgument("documentType") {
                type = NavType.EnumType(IdentityDocumentType::class.java)
            })
        ) { entry ->
            val identityDocumentType = entry.arguments?.getSerializable("documentType") as IdentityDocumentType
            UploadCaptureScreen(identityViewModel, identityDocumentType)
        }

        composable(
            IdentityDestinations.DOCUMENT_CAPTURE_METHOD_MANUAL_ROUTE,
            arguments = listOf(navArgument("documentType") {
                type = NavType.EnumType(IdentityDocumentType::class.java)
            })
        ) { entry ->
            val identityDocumentType = entry.arguments?.getSerializable("documentType") as IdentityDocumentType
            ManualCaptureScreen(identityViewModel, identityDocumentType)
        }
    }
}