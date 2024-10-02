package io.falu.identity.screens.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_UPLOAD_CAPTURE
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.ObserveVerificationAndCompose

@Composable
internal fun ManualCaptureScreen(
    viewModel: IdentityVerificationViewModel,
    documentType: IdentityDocumentType,
    navigateToSelfie: () -> Unit,
    navigateToTaxPin: () -> Unit,
    navigateToRequirementErrors: () -> Unit,
    navigateToConfirmation: () -> Unit,
    navigateToError: (Throwable?) -> Unit
) {
    val context = LocalContext.current;
    val verificationResponse by viewModel.verification.observeAsState()
    val documentDisposition by viewModel.documentUploadDisposition.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_UPLOAD_CAPTURE)
            )
        }

        Column {
            DocumentCaptureView(
                title = stringResource(
                    id = R.string.upload_document_capture_title,
                    stringResource(documentType.titleRes)
                ),
                documentType = documentType,
                isFrontUploaded = documentDisposition?.isFrontUpload ?: false,
                isBackUploaded = documentDisposition?.isBackUploaded ?: false,
                onFront = { viewModel.imageHandler.captureImageFront(context) },
                onBack = { viewModel.imageHandler.captureImageBack(context) })

            Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.content_padding_normal))) {
                LoadingButton(
                    text = stringResource(R.string.button_continue),
                    enabled = documentDisposition?.isBothUploadLoad ?: false
                ) {
                    if (documentDisposition == null) return@LoadingButton

                    val uploadRequest = documentDisposition!!.generateVerificationUploadRequest(documentType)

                    val options = VerificationUpdateOptions(document = uploadRequest.document)


                    viewModel.updateVerification(
                        options,
                        onSuccess = {
                            viewModel.attemptDocumentSubmission(
                                verification = verification,
                                verificationRequest = uploadRequest,
                                navigateToSelfie = navigateToSelfie,
                                navigateToTaxPin = navigateToTaxPin,
                                navigateToRequirementErrors = navigateToRequirementErrors,
                                onSubmitted = navigateToConfirmation,
                                onError = { navigateToError(it) }
                            )
                        },
                        onError = { navigateToError(it) },
                        onFailure = { navigateToError(it) }
                    )
                }
            }
        }
    }
}