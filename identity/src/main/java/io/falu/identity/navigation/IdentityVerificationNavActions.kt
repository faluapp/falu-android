package io.falu.identity.navigation

import androidx.navigation.NavController
import io.falu.identity.api.models.IdentityDocumentType

internal class IdentityVerificationNavActions(private val navController: NavController) {

    fun navigateToWelcome() {
        navController.navigateTo(WelcomeDestination())
    }

    fun navigateToSupport() {
        navController.navigateTo(SupportDestination())
    }

    fun navigateToDocumentSelection() {
        navController.navigateTo(DocumentSelectionDestination())
    }

    fun navigateToConfirmation() {
        navController.navigateTo(ConfirmationDestination())
    }

    fun navigateToDocumentCaptureMethods(documentType: IdentityDocumentType) {
        navController.navigateTo(DocumentCaptureDestination(documentType))
    }

    fun navigateToScanCapture(documentType: IdentityDocumentType) {
        navController.navigateTo(ScanCaptureDestination(documentType))
    }

    fun navigateToManualCapture(documentType: IdentityDocumentType) {
        navController.navigateTo(ManualCaptureDestination(documentType))
    }

    fun navigateToUploadCapture(documentType: IdentityDocumentType) {
        navController.navigateTo(UploadCaptureDestination(documentType))
    }

    fun navigateToSelfie() {
        navController.navigateTo(SelfieDestination())
    }

    fun navigateToError(error: ErrorDestination) {
        navController.navigateTo(error)
    }

    fun navigateToCameraPermissionDenied() {
        navController.navigateTo(CameraPermissionDeniedDestination())
    }
}