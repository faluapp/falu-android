package io.falu.identity.screens.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.capture.scan.DocumentScanViewModel
import io.falu.identity.capture.scan.DocumentScanner
import io.falu.identity.scan.ScanDisposition

@Composable
internal fun DocumentScanLaunchedEffect(
    identityViewModel: IdentityVerificationViewModel,
    documentScanViewModel: DocumentScanViewModel,
    verificationCapture: VerificationCapture,
    scanner: DocumentScanner,
    scanType: ScanDisposition.DocumentScanType,
    onScanComplete: (DocumentDetectionOutput) -> Unit,
    onTimeout: () -> Unit,
    onScannerReady: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(documentScanViewModel) {
        identityViewModel.documentDetectorModelFile.observe(lifecycleOwner) { model ->
            if (model != null) {
                scanner.addAnalyzers(model, verificationCapture, scanType, identityViewModel.modelPerformanceMonitor)
                documentScanViewModel.initializeScanner(scanner)
                onScannerReady()
            }
        }

        documentScanViewModel.documentScanCompleteDisposition.observe(lifecycleOwner) { result ->
            if (result.disposition is ScanDisposition.Timeout) {
                identityViewModel.reportTelemetry(
                    identityViewModel
                        .analyticsRequestBuilder
                        .documentScanTimeOut(scanType = (result.disposition as ScanDisposition.Timeout).type)
                )
                onTimeout()
            } else if (result.disposition is ScanDisposition.Completed) {
                val output = result.output as DocumentDetectionOutput
                reportSuccessfulScanTelemetry(
                    identityViewModel,
                    result.disposition as ScanDisposition.Completed,
                    output
                )
                onScanComplete(output)
            }
        }
    }
}

private fun reportSuccessfulScanTelemetry(
    identityViewModel: IdentityVerificationViewModel,
    scanDisposition: ScanDisposition,
    output: DocumentDetectionOutput
) {
    val telemetryDisposition = if (scanDisposition.type.isFront) {
        AnalyticsDisposition(frontModelScore = output.score, scanType = scanDisposition.type)
    } else {
        AnalyticsDisposition(backModelScore = output.score, scanType = scanDisposition.type)
    }

    identityViewModel.modifyAnalyticsDisposition(disposition = telemetryDisposition)
}