package io.falu.identity.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.capture.scan.DocumentScanViewModel
import io.falu.identity.screens.ConfirmationScreen
import io.falu.identity.screens.DocumentCaptureMethodsScreen
import io.falu.identity.screens.DocumentSelectionScreen
import io.falu.identity.screens.InitialLoadingScreen
import io.falu.identity.screens.WelcomeScreen
import io.falu.identity.screens.capture.ManualCaptureScreen
import io.falu.identity.screens.capture.ScanCaptureScreen
import io.falu.identity.screens.capture.UploadCaptureScreen
import io.falu.identity.screens.error.ErrorScreen
import io.falu.identity.screens.error.ErrorScreenButton
import io.falu.identity.screens.selfie.SelfieScreen
import io.falu.identity.selfie.FaceScanViewModel

@Composable
internal fun IdentityNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    identityViewModel: IdentityVerificationViewModel,
    documentScanViewModel: DocumentScanViewModel,
    faceScanViewModel: FaceScanViewModel,
    startDestination: String = InitialDestination.ROUTE.route,
    navActions: IdentityVerificationNavActions = remember(navController) {
        IdentityVerificationNavActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(InitialDestination.ROUTE.route) {
            InitialLoadingScreen(identityViewModel = identityViewModel, navActions = navActions)
        }

        composable(WelcomeDestination.ROUTE.route) {
            WelcomeScreen(viewModel = identityViewModel, navActions = navActions)
        }

        composable(ConfirmationDestination.ROUTE.route) {
            ConfirmationScreen(viewModel = identityViewModel)
        }

        composable(DocumentSelectionDestination.ROUTE.route) {
            DocumentSelectionScreen(
                viewModel = identityViewModel,
                navigateToCaptureMethods = { navActions.navigateToDocumentCaptureMethods(it) },
                navigateToError = {})
        }

        composable(SelfieDestination.ROUTE.route) {
            SelfieScreen(
                viewModel = identityViewModel,
                faceScanViewModel = faceScanViewModel,
                navActions = navActions
            )
        }

        composable(
            DocumentCaptureDestination.ROUTE.route,
            arguments = DocumentCaptureDestination.ROUTE.arguments
        ) { entry ->
            DocumentCaptureMethodsScreen(
                identityViewModel,
                DocumentCaptureDestination.identityDocumentType(entry),
                navigateToCaptureMethod = {
                    when (it) {
                        UploadMethod.AUTO -> navActions.navigateToScanCapture(
                            documentType = DocumentCaptureDestination.identityDocumentType(entry)
                        )

                        UploadMethod.MANUAL -> navActions.navigateToManualCapture(
                            documentType = DocumentCaptureDestination.identityDocumentType(entry)
                        )

                        UploadMethod.UPLOAD -> navActions.navigateToUploadCapture(
                            documentType = DocumentCaptureDestination.identityDocumentType(entry)
                        )
                    }
                })
        }

        composable(
            UploadCaptureDestination.ROUTE.route,
            arguments = UploadCaptureDestination.ROUTE.arguments
        ) { entry ->
            UploadCaptureScreen(
                viewModel = identityViewModel,
                documentType = UploadCaptureDestination.identityDocumentType(entry),
                navActions = navActions
            )
        }

        composable(
            ManualCaptureDestination.ROUTE.route,
            arguments = ManualCaptureDestination.ROUTE.arguments
        ) { entry ->
            ManualCaptureScreen(
                viewModel = identityViewModel,
                documentType = ManualCaptureDestination.identityDocumentType(entry),
                navActions = navActions
            )
        }

        composable(
            ScanCaptureDestination.ROUTE.route,
            arguments = ScanCaptureDestination.ROUTE.arguments
        ) { entry ->
            ScanCaptureScreen(
                viewModel = identityViewModel,
                documentScanViewModel = documentScanViewModel,
                documentType = ScanCaptureDestination.identityDocumentType(entry),
                navActions = navActions
            )
        }

        composable(
            ErrorDestination.ROUTE.route,
            arguments = ErrorDestination.ROUTE.arguments
        ) { entry ->
            ErrorScreen(
                title = ErrorDestination.errorTitle(entry) ?: "",
                desc = ErrorDestination.errorDescription(entry) ?: "",
                message = ErrorDestination.errorMessage(entry),
                primaryButton = ErrorScreenButton(
                    text = ErrorDestination.backButtonText(entry) ?: "",
                    onClick = {}
                ),
                secondaryButton = if (ErrorDestination.cancelFlow(entry)) {
                    ErrorScreenButton(text = stringResource(R.string.button_cancel), onClick = {})
                } else {
                    null
                }
            )
        }
    }
}