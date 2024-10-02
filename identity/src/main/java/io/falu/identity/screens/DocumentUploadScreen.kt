package io.falu.identity.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_UPLOAD_CAPTURE
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.ObserveVerificationAndCompose
import io.falu.identity.ui.theme.IdentityTheme

@Composable
internal fun DocumentUploadScreen(
    viewModel: IdentityVerificationViewModel,
    documentType: IdentityDocumentType,
) {
    val verificationResponse by viewModel.verification.observeAsState()
    val documentDisposition by viewModel.documentUploadDisposition.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_UPLOAD_CAPTURE)
            )
        }

        Column {
            DocumentUploadView(
                documentType,
                documentDisposition?.isFrontUpload ?: false,
                documentDisposition?.isBackUploaded ?: false,
                onFront = { viewModel.imageHandler.pickImageFront() },
                onBack = { viewModel.imageHandler.pickImageBack() })

            LoadingButton(
                text = stringResource(R.string.button_continue),
                enabled = documentDisposition?.isBothUploadLoad ?: false
            ) { }
        }
    }
}

@Composable
private fun DocumentUploadView(
    documentType: IdentityDocumentType,
    isFrontUploaded: Boolean,
    isBackUploaded: Boolean,
    onFront: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
    ) {
        Text(
            text = stringResource(id = R.string.upload_document_capture_title, stringResource(documentType.titleRes)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.element_spacing_normal)),
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.content_padding_normal)),
        ) {
            DocumentCard(
                title = stringResource(
                    R.string.upload_document_capture_document_font,
                    stringResource(documentType.titleRes)
                ),
                onSelectClicked = { onFront() },
                isUploaded = isFrontUploaded
            )

            if (documentType != IdentityDocumentType.PASSPORT) {
                DocumentCard(
                    title = stringResource(
                        R.string.upload_document_capture_document_back,
                        stringResource(documentType.titleRes)
                    ),
                    onSelectClicked = { onBack() },
                    isUploaded = isBackUploaded
                )
            }
        }
    }
}

@Composable
private fun DocumentCard(
    title: String,
    onSelectClicked: () -> Unit,
    isUploaded: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.content_padding_normal)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.element_spacing_normal))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.element_spacing_normal)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimensionResource(R.dimen.element_spacing_normal))
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isUploaded) {
                    TextButton(onClick = onSelectClicked) {
                        Text(text = stringResource(id = R.string.button_select))
                    }
                }

                if (isUploaded) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.content_padding_normal))
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DocumentUploadScreenPreview() {
    IdentityTheme {
        DocumentUploadView(
            documentType = IdentityDocumentType.IDENTITY_CARD,
            isFrontUploaded = true,
            isBackUploaded = true,
            onFront = {},
            onBack = {})
    }
}