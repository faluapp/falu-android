package io.falu.identity.navigation

import androidx.navigation.NavController
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.requirements.RequirementError
import io.falu.identity.scan.ScanDisposition

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

    fun navigateToErrorWithRequirementErrors(fromRoute: String, error: RequirementError) {
        navController.navigateWithRequirementErrors(fromRoute, error)
    }

    fun navigateToErrorWithApiExceptions(throwable: Throwable?) {
        navController.navigateToErrorWithApiExceptions(throwable)
    }

    fun navigateToErrorWithFailure(throwable: Throwable?) {
        navController.navigateToErrorWithFailure(throwable)
    }

    fun navigateToErrorWithScreenTimeout(scanType: ScanDisposition.DocumentScanType?) {
        navController.navigateTo(ScanTimeoutDestination(scanType))
    }

    fun navigateToCameraPermissionDenied() {
        navController.navigateTo(CameraPermissionDeniedDestination())
    }

    fun navigateToDocumentVerification(documentType: IdentityDocumentType) {
        navController.navigateTo(DocumentVerificationDestination(documentType))
    }

    fun navigateToTaxPin() {
        navController.navigateTo(TaxPinDestination())
    }
}