package io.falu.identity.screens.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import io.falu.identity.viewModel.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_UPLOAD_CAPTURE
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.navigation.UploadCaptureDestination
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.ObserveVerificationAndCompose

@Composable
internal fun UploadCaptureScreen(
    viewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    documentType: IdentityDocumentType
) {
    val context = LocalContext.current
    val verificationResponse by viewModel.verification.observeAsState()
    val documentDisposition by viewModel.documentUploadDisposition.observeAsState()

    var frontLoading by remember { mutableStateOf(documentDisposition?.isFrontUpload ?: false) }
    var backLoading by remember { mutableStateOf(documentDisposition?.isBackUploaded ?: false) }

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
                isFrontLoading = frontLoading,
                isBackLoading = backLoading,
                onFront = {
                    frontLoading = true
                    viewModel.imageHandler.pickImageFront()
                },
                onBack = {
                    backLoading = true
                    viewModel.imageHandler.pickImageBack()
                }
            )

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
                                fromRoute = UploadCaptureDestination.ROUTE.route,
                                verification = verification,
                                navActions = navActions,
                                verificationRequest = uploadRequest,
                            )
                        },
                        onError = { throwable -> navActions.navigateToErrorWithApiExceptions(throwable) },
                        onFailure = { throwable -> navActions.navigateToErrorWithFailure(throwable) }
                    )
                }
            }
        }
    }
}