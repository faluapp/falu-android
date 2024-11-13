package io.falu.identity.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.falu.identity.ContractArgs
import io.falu.identity.FallbackUrlCallback
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.R
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.screens.ConfirmationScreen
import io.falu.identity.screens.DocumentCaptureMethodsScreen
import io.falu.identity.screens.DocumentSelectionScreen
import io.falu.identity.screens.DocumentVerificationScreen
import io.falu.identity.screens.InitialLoadingScreen
import io.falu.identity.screens.SupportScreen
import io.falu.identity.screens.TaxPinVerificationScreen
import io.falu.identity.screens.WelcomeScreen
import io.falu.identity.screens.capture.ManualCaptureScreen
import io.falu.identity.screens.capture.ScanCaptureScreen
import io.falu.identity.screens.capture.UploadCaptureScreen
import io.falu.identity.screens.error.ErrorScreen
import io.falu.identity.screens.error.ErrorScreenButton
import io.falu.identity.screens.selfie.SelfieScreen
import io.falu.identity.ui.IdentityVerificationBaseScreen
import io.falu.identity.utils.openAppSettings
import io.falu.identity.viewModel.DocumentScanViewModel
import io.falu.identity.viewModel.FaceScanViewModel
import io.falu.identity.viewModel.IdentityVerificationViewModel

@Composable
internal fun IdentityNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    identityViewModel: IdentityVerificationViewModel,
    documentScanViewModel: DocumentScanViewModel,
    faceScanViewModel: FaceScanViewModel,
    verificationResultCallback: IdentityVerificationResultCallback,
    contractArgs: ContractArgs,
    fallbackUrlCallback: FallbackUrlCallback,
    startDestination: String = InitialDestination.ROUTE.route,
    navActions: IdentityVerificationNavActions = remember(navController) {
        IdentityVerificationNavActions(navController)
    },
    onNavControllerCreated: (NavController) -> Unit
) {
    IdentityVerificationBaseScreen(
        viewModel = identityViewModel,
        contractArgs = contractArgs,
        navigateToSupport = { navActions.navigateToSupport() }
    ) {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            onNavControllerCreated(navController)
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(InitialDestination.ROUTE.route) {
                InitialLoadingScreen(
                    identityViewModel = identityViewModel,
                    navActions = navActions,
                    fallbackUrlCallback = fallbackUrlCallback
                )
            }

            composable(SupportDestination.ROUTE.route) {
                SupportScreen(identityViewModel = identityViewModel)
            }

            composable(WelcomeDestination.ROUTE.route) {
                WelcomeScreen(
                    viewModel = identityViewModel,
                    navActions = navActions,
                    verificationResultCallback = verificationResultCallback
                )
            }

            composable(ConfirmationDestination.ROUTE.route) {
                ConfirmationScreen(viewModel = identityViewModel, callback = verificationResultCallback)
            }

            composable(DocumentSelectionDestination.ROUTE.route) {
                DocumentSelectionScreen(viewModel = identityViewModel, navActions = navActions)
            }

            composable(TaxPinDestination.ROUTE.route) {
                TaxPinVerificationScreen(viewModel = identityViewModel, navActions = navActions)
            }

            composable(SelfieDestination.ROUTE.route) {
                SelfieScreen(
                    viewModel = identityViewModel,
                    faceScanViewModel = faceScanViewModel,
                    navActions = navActions
                )
            }

            composable(
                DocumentVerificationDestination.ROUTE.route,
                arguments = DocumentVerificationDestination.ROUTE.arguments
            ) {
                DocumentVerificationScreen(
                    viewModel = identityViewModel,
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
                    }
                )
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
                    primaryButton = ErrorDestination.primaryButtonOptions(entry)
                        ?.let { (buttonText, buttonRequirement) ->
                            ErrorScreenButton(
                                text = buttonText,
                                onClick = {
                                    if (buttonRequirement != null) {
                                        // NOOP
                                    } else {
                                        navController.resetAndNavigateUp(identityViewModel)
                                    }
                                }
                            )
                        },
                    secondaryButton = ErrorScreenButton(
                        text = ErrorDestination.backButtonText(entry) ?: stringResource(R.string.button_cancel),
                        onClick = {
                            if (ErrorDestination.cancelFlow(entry)) {
                                verificationResultCallback.onFinishWithResult(IdentityVerificationResult.Canceled)
                            } else {
                                val destination = ErrorDestination.backButtonDestination(entry)
                                if (destination.isNullOrEmpty()) {
                                    navActions.navigateToWelcome()
                                } else {
                                    var shouldContinueNavigateUp = true
                                    while (
                                        shouldContinueNavigateUp &&
                                        navController.currentDestination?.route?.substringBefore("?") !=
                                        destination
                                    ) {
                                        shouldContinueNavigateUp = navController.resetAndNavigateUp(identityViewModel)
                                    }
                                }
                            }
                        }
                    )
                )
            }

            composable(CameraPermissionDeniedDestination.ROUTE.route) {
                ErrorScreen(
                    title = stringResource(R.string.permission_explanation_title),
                    desc = stringResource(R.string.permission_explanation_camera),
                    primaryButton = ErrorScreenButton(
                        text = stringResource(R.string.button_app_settings),
                        onClick = { context.openAppSettings() }
                    )
                )
            }

            composable(
                ScanTimeoutDestination.ROUTE.route,
                arguments = ScanTimeoutDestination.ROUTE.arguments
            ) { entry ->
                val scanType = ScanTimeoutDestination.scanType(entry)

                ErrorScreen(
                    title = context.getString(R.string.error_title_scan_capture),
                    desc = context.getString(R.string.error_description_scan_capture),
                    message = if (scanType == ScanDisposition.DocumentScanType.SELFIE) {
                        context.getString(R.string.error_description_selfie_capture)
                    } else {
                        context.getString(R.string.error_message_scan_capture)
                    },
                    secondaryButton = ErrorScreenButton(
                        text = stringResource(R.string.button_try_again),
                        onClick = {
                            if (scanType != null) {
                                navController.navigateTo(scanType.toScanDestination())
                            }
                        }
                    )
                )
            }
        }
    }
}