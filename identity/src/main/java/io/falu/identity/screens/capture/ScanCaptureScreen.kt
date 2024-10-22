package io.falu.identity.screens.capture

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_AUTO_CAPTURE
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.camera.CameraView
import io.falu.identity.capture.AbstractCaptureFragment.Companion.getIdentityDocumentName
import io.falu.identity.capture.scan.DocumentScanViewModel
import io.falu.identity.capture.scan.DocumentScanner
import io.falu.identity.capture.scan.ScanCaptureFragment.Companion.getScanType
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.navigation.ErrorDestination
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.ObserveVerificationAndCompose

@Composable
internal fun ScanCaptureScreen(
    viewModel: IdentityVerificationViewModel,
    documentScanViewModel: DocumentScanViewModel,
    navActions: IdentityVerificationNavActions,
    documentType: IdentityDocumentType,
) {
    val context = LocalContext.current
    val verificationResponse by viewModel.verification.observeAsState()
    val documentDisposition by viewModel.documentUploadDisposition.observeAsState()

    var uploadFront by remember { mutableStateOf(false) }
    var uploadBack by remember { mutableStateOf(false) }

    var frontLoading by remember { mutableStateOf(documentDisposition?.isFrontUpload ?: false) }
    var backLoading by remember { mutableStateOf(documentDisposition?.isBackUploaded ?: false) }

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_AUTO_CAPTURE)
            )
        }

        Box(modifier = Modifier.wrapContentHeight()) {
            when {
                uploadFront -> {
                    DocumentSideCapture(
                        identityViewModel = viewModel,
                        documentScanViewModel = documentScanViewModel,
                        documentType = documentType,
                        scanType = documentType.getScanType().first,
                        capture = verification.capture,
                        onUpload = {
                            frontLoading = true
                            uploadDocument(
                                context,
                                viewModel,
                                navActions,
                                it,
                                DocumentSide.FRONT,
                                onLoad = { loading -> frontLoading = loading }
                            )
                            uploadFront = false
                        },
                        onScanTimeOut = {
                            navActions.navigateToError(
                                ErrorDestination.withCameraTimeout(
                                    title = context.getString(R.string.error_title_scan_capture),
                                    desc = context.getString(R.string.error_description_scan_capture),
                                    message = context.getString(R.string.error_message_scan_capture),
                                    backButtonText = context.getString(R.string.button_try_again),
                                    backButtonDestination = "",
                                )
                            )
                        }
                    )
                }

                uploadBack -> {
                    documentType.getScanType().second?.let {
                        DocumentSideCapture(
                            viewModel,
                            documentScanViewModel,
                            documentType,
                            it,
                            verification.capture,
                            onUpload = { output ->
                                backLoading = true
                                uploadDocument(
                                    context,
                                    viewModel,
                                    navActions,
                                    output,
                                    DocumentSide.BACK,
                                    onLoad = { loading -> backLoading = loading }
                                )
                                uploadBack = false
                            },
                            onScanTimeOut = {
                                navActions.navigateToError(
                                    ErrorDestination.withCameraTimeout(
                                        title = context.getString(R.string.error_title_scan_capture),
                                        desc = context.getString(R.string.error_description_scan_capture),
                                        message = context.getString(R.string.error_message_scan_capture),
                                        backButtonText = context.getString(R.string.button_try_again),
                                        backButtonDestination = "",
                                    )
                                )
                            }
                        )
                    }
                }

                else -> {
                    Column {
                        DocumentCaptureView(
                            title = stringResource(
                                id = R.string.upload_document_capture_title,
                                stringResource(documentType.titleRes)
                            ),
                            documentType = documentType,
                            isFrontUploaded = documentDisposition?.isFrontUpload ?: false,
                            isBackUploaded = documentDisposition?.isBackUploaded ?: false,
                            isFrontLoading = frontLoading,
                            isBackLoading = backLoading,
                            onFront = { uploadFront = true },
                            onBack = { uploadBack = true })

                        Column(
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
                        ) {
                            LoadingButton(
                                text = stringResource(R.string.button_continue),
                                enabled = documentDisposition?.isBothUploadLoad ?: false
                            ) {
                                if (documentDisposition == null) return@LoadingButton

                                val uploadRequest =
                                    documentDisposition!!.generateVerificationUploadRequest(documentType)

                                val options = VerificationUpdateOptions(document = uploadRequest.document)

                                viewModel.updateVerification(
                                    options,
                                    onSuccess = {
                                        viewModel.attemptDocumentSubmission(
                                            context = context,
                                            verification = verification,
                                            navActions = navActions,
                                            verificationRequest = uploadRequest,
                                        )
                                    },
                                    onError = {
                                        navActions.navigateToError(
                                            ErrorDestination.withApiFailure(
                                                title = context.getString(R.string.error_title),
                                                desc = context.getString(R.string.error_title_unexpected_error),
                                                backButtonText = context.getString(R.string.button_rectify),
                                                backButtonDestination = "",
                                                throwable = it
                                            )
                                        )
                                    },
                                    onFailure = {
                                        navActions.navigateToError(
                                            ErrorDestination.withApiFailure(
                                                title = context.getString(R.string.error_title),
                                                desc = context.getString(R.string.error_title_unexpected_error),
                                                backButtonText = context.getString(R.string.button_rectify),
                                                backButtonDestination = "",
                                                throwable = it
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentSideCapture(
    identityViewModel: IdentityVerificationViewModel,
    documentScanViewModel: DocumentScanViewModel,
    documentType: IdentityDocumentType,
    scanType: ScanDisposition.DocumentScanType,
    capture: VerificationCapture,
    onUpload: (DocumentDetectionOutput) -> Unit,
    onScanTimeOut: () -> Unit
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current

    val documentScanDisposition by documentScanViewModel.documentScanDisposition.observeAsState()
    var detectionOutput by remember { mutableStateOf<DocumentDetectionOutput?>(null) }

    val scanner = remember {
        DocumentScanner(context)
    }

    LaunchedEffect(Unit) {
        detectionOutput = null
        documentScanViewModel.resetScanDispositions()
    }

    val newDisplayState by remember {
        derivedStateOf {
            documentScanDisposition?.disposition
        }
    }

    LaunchedEffect(newDisplayState) {
        if (newDisplayState is ScanDisposition.Completed) {
            documentScanViewModel.stopScan(owner)
        }
    }

    val message = when (newDisplayState) {
        is ScanDisposition.Start -> {
            stringResource(
                R.string.scan_capture_text_scan_message,
                documentType.getIdentityDocumentName(context)
            )
        }

        is ScanDisposition.Detected -> {
            stringResource(R.string.scan_capture_text_document_detected)
        }

        is ScanDisposition.Desired -> {
            stringResource(R.string.scan_capture_text_document_scan_completed)
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

    DocumentScanLaunchedEffect(
        identityViewModel = identityViewModel,
        verificationCapture = capture,
        documentScanViewModel = documentScanViewModel,
        onScanComplete = { detectionOutput = it },
        scanType = scanType,
        scanner = scanner,
        onTimeout = { onScanTimeOut() }
    ) {
        documentScanViewModel.startScan(owner, capture, scanType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
            .padding(bottom = dimensionResource(R.dimen.element_spacing_normal))
    ) {

        Text(
            text = stringResource(
                if (scanType.isFront) {
                    R.string.scan_capture_text_document_side_front
                } else {
                    R.string.scan_capture_text_document_side_back
                },
                stringResource(documentType.titleRes)
            ),
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
                            documentScanViewModel.startScan(owner, capture, scanType)
                        }
                    )
                }

                else -> {
                    Column {
                        Text(
                            text = message.ifEmpty {
                                stringResource(
                                    R.string.scan_capture_text_scan_message,
                                    documentType.getIdentityDocumentName(context)
                                )
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = dimensionResource(R.dimen.element_spacing_normal)),
                            textAlign = TextAlign.Center
                        )

                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            factory = { ctx ->
                                CameraView(ctx).apply {
                                    bindLifecycle(owner)
                                    lensFacing = CameraSelector.LENS_FACING_BACK
                                    cameraViewType = if (documentType != IdentityDocumentType.PASSPORT)
                                        CameraView.CameraViewType.ID
                                    else
                                        CameraView.CameraViewType.PASSPORT
                                }
                            },
                            update = {
                                scanner.onUpdateCameraView(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun uploadDocument(
    context: Context,
    identityViewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    output: DocumentDetectionOutput,
    documentSide: DocumentSide,
    onLoad: (Boolean) -> Unit,
) {
    identityViewModel.uploadScannedDocument(
        output.bitmap,
        documentSide,
        output.score,
        onError = {
            onLoad(false)
            navActions.navigateToError(
                ErrorDestination.withApiFailure(
                    title = context.getString(R.string.error_title),
                    desc = context.getString(R.string.error_title_unexpected_error),
                    backButtonText = context.getString(R.string.button_rectify),
                    backButtonDestination = "",
                    throwable = it
                )
            )
        },
        onFailure = {
            onLoad(false)
            navActions.navigateToError(
                ErrorDestination.withApiFailure(
                    title = context.getString(R.string.error_title),
                    desc = context.getString(R.string.error_title_unexpected_error),
                    backButtonText = context.getString(R.string.button_rectify),
                    backButtonDestination = "",
                    throwable = it
                )
            )
        }
    )
}