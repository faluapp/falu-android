package io.falu.identity.screens.selfie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.selfie.FaceScanViewModel
import io.falu.identity.selfie.FaceScanner

@Composable
internal fun SelfieCameraLaunchEffect(
    identityViewModel: IdentityVerificationViewModel,
    faceScanViewModel: FaceScanViewModel,
    scanner: FaceScanner,
    capture: VerificationCapture,
    onScanComplete: (FaceDetectionOutput) -> Unit,
    onTimeout: () -> Unit,
    onScannerReady: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(faceScanViewModel) {
        identityViewModel.faceDetectorModelFile.observe(lifecycleOwner) { model ->
            if (model != null) {
                scanner.addAnalyzers(
                    model = model,
                    capture = capture,
                    ScanDisposition.DocumentScanType.SELFIE,
                    performanceMonitor = identityViewModel.modelPerformanceMonitor
                )
                faceScanViewModel.initializeScanner(scanner)
                onScannerReady()
            }
        }

        faceScanViewModel.faceScanCompleteDisposition.observe(lifecycleOwner) { result ->
            if (result.disposition is ScanDisposition.Completed) {
                val output = result.output as FaceDetectionOutput
                identityViewModel.modifyAnalyticsDisposition(
                    disposition = AnalyticsDisposition(selfieModelScore = output.score)
                )
                onScanComplete(output)
            } else if (result.disposition is ScanDisposition.Timeout) {
                identityViewModel.reportTelemetry(identityViewModel.analyticsRequestBuilder.selfieScanTimeOut())
                onTimeout()
            }
        }
    }
}