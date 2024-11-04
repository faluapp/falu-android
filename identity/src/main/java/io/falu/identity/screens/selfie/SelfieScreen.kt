package io.falu.identity.screens.selfie

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.falu.core.models.FaluFile
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_SELFIE
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.api.models.verification.VerificationSelfieUpload
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraView
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.screens.CameraPermissionLaunchEffect
import io.falu.identity.screens.capture.CapturePreview
import io.falu.identity.selfie.FaceScanViewModel
import io.falu.identity.selfie.FaceScanner
import io.falu.identity.ui.ObserveVerificationAndCompose

internal const val SELFIE_VIEW_ASPECT_RATIO = 1f

@Composable
internal fun SelfieScreen(
    viewModel: IdentityVerificationViewModel,
    faceScanViewModel: FaceScanViewModel,
    navActions: IdentityVerificationNavActions
) {
    val context = LocalContext.current
    val verificationResponse by viewModel.verification.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_SELFIE)
            )
        }

        CameraPermissionLaunchEffect(
            onPermissionDenied = { navActions.navigateToCameraPermissionDenied() },
            onPermissionGranted = {}
        )

        SelfieCaptureView(
            identityViewModel = viewModel,
            faceScanViewModel = faceScanViewModel,
            capture = verification.capture,
            onUpload = {
                uploadSelfie(
                    context = context,
                    identityViewModel = viewModel,
                    navActions = navActions,
                    verification = verification,
                    output = it
                )
            },
            onScanTimeOut = {
                navActions.navigateToErrorWithScreenTimeout(ScanDisposition.DocumentScanType.SELFIE)
            }
        )
    }
}

@Composable
private fun SelfieCaptureView(
    identityViewModel: IdentityVerificationViewModel,
    faceScanViewModel: FaceScanViewModel,
    capture: VerificationCapture,
    onUpload: (FaceDetectionOutput) -> Unit,
    onScanTimeOut: () -> Unit
) {
    val owner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val faceScanDisposition by faceScanViewModel.faceScanDisposition.observeAsState()
    var detectionOutput by remember { mutableStateOf<FaceDetectionOutput?>(null) }

    val scanner = remember { FaceScanner(context) }

    LaunchedEffect(Unit) {
        detectionOutput = null
        faceScanViewModel.resetScanDispositions()
    }

    val newDisplayState by remember {
        derivedStateOf {
            faceScanDisposition?.disposition
        }
    }

    LaunchedEffect(newDisplayState) {
        if (newDisplayState is ScanDisposition.Completed) {
            faceScanViewModel.stopScan(owner)
        }
    }

    val message = when (newDisplayState) {
        is ScanDisposition.Start -> {
            stringResource(R.string.selfie_text_scan_message)
        }

        is ScanDisposition.Detected -> {
            stringResource(R.string.selfie_text_face_detected)
        }

        is ScanDisposition.Desired -> {
            stringResource(R.string.selfie_text_selfie_scan_completed)
        }

        is ScanDisposition.Undesired -> {
            ""
        }

        is ScanDisposition.Completed -> {
            ""
        }

        is ScanDisposition.Timeout, null -> {
            ""
        }
    }

    SelfieCameraLaunchEffect(
        identityViewModel = identityViewModel,
        faceScanViewModel = faceScanViewModel,
        scanner = scanner,
        capture = capture,
        onScanComplete = { detectionOutput = it },
        onTimeout = { onScanTimeOut() }
    ) {
        faceScanViewModel.startScan(owner, capture)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
            .padding(bottom = dimensionResource(R.dimen.element_spacing_normal))
    ) {
        Text(
            text = stringResource(R.string.selfie_text_take_selfie),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.element_spacing_normal)),
            textAlign = TextAlign.Center
        )

        Box {
            when {
                detectionOutput != null -> {
                    CapturePreview(
                        bitmap = detectionOutput!!.bitmap,
                        onContinue = {
                            onUpload(detectionOutput!!)
                        },
                        onDiscard = {
                            detectionOutput = null
                            faceScanViewModel.resetScanDispositions()
                            faceScanViewModel.startScan(owner, capture)
                        }
                    )
                }

                else -> {
                    Column {
                        Text(
                            text = message.ifEmpty { stringResource(R.string.selfie_text_scan_message) },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = dimensionResource(R.dimen.element_spacing_normal)),
                            textAlign = TextAlign.Center
                        )

                        SelfieCameraView(owner, scanner)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelfieCameraView(owner: LifecycleOwner, scanner: FaceScanner) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .aspectRatio(SELFIE_VIEW_ASPECT_RATIO)
            .padding(horizontal = dimensionResource(id = R.dimen.content_padding_normal))
            .clip(ShapeModifier)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                CameraView(ctx).apply {
                    bindLifecycle(owner)
                    lensFacing = CameraSelector.LENS_FACING_FRONT
                    cameraViewType = CameraView.CameraViewType.FACE
                }
            },
            update = {
                scanner.onUpdateCameraView(it)
            }
        )
    }
}

private fun uploadSelfie(
    context: Context,
    identityViewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    verification: Verification,
    output: FaceDetectionOutput,
) {
    identityViewModel.uploadSelfieImage(
        output.bitmap,
        onSuccess = {
            submitSelfieAndUploadedDocuments(
                context = context,
                identityViewModel = identityViewModel,
                navActions = navActions,
                verification = verification,
                file = it
            )
        },
        onFailure = { throwable ->
            navActions.navigateToErrorWithFailure(throwable)
        },
        onError = { throwable ->
            navActions.navigateToErrorWithApiExceptions(throwable)
        }
    )
}

private fun submitSelfieAndUploadedDocuments(
    context: Context,
    identityViewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    verification: Verification,
    file: FaluFile
) {
    val selfie = VerificationSelfieUpload(
        UploadMethod.AUTO,
        file = file.id,
        variance = 0F
    )

    val options = VerificationUpdateOptions(selfie = selfie)
    val uploadRequest = VerificationUploadRequest(selfie = selfie)

    identityViewModel.updateVerification(
        options,
        onSuccess = {
            identityViewModel.attemptDocumentSubmission(
                context = context,
                navActions = navActions,
                verification = verification,
                verificationRequest = uploadRequest,
            )
        },
        onFailure = { throwable ->
            navActions.navigateToErrorWithFailure(throwable)
        },
        onError = { throwable ->
            navActions.navigateToErrorWithApiExceptions(throwable)
        }
    )
}

private val ShapeModifier = GenericShape { size, _ ->
    addOval(Rect(Offset.Zero, size))
}