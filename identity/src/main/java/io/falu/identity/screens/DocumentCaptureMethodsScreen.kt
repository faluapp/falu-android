package io.falu.identity.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_DOCUMENT_CAPTURE_METHODS
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.ui.ObserveVerificationAndCompose
import io.falu.identity.ui.theme.IdentityTheme

@Composable
internal fun DocumentCaptureMethodsScreen(
    viewModel: IdentityVerificationViewModel,
    documentType: IdentityDocumentType,
    navigateToCaptureMethod: (UploadMethod) -> Unit
) {
    val verificationResponse by viewModel.verification.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_DOCUMENT_CAPTURE_METHODS)
            )
        }

        DocumentSelectionView(
            documentType,
            allowUploads = true,
            onCaptureMethod = { navigateToCaptureMethod(it) })
    }
}

@Composable
private fun DocumentSelectionView(
    documentType: IdentityDocumentType,
    allowUploads: Boolean = true,
    onCaptureMethod: (UploadMethod) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
    ) {
        Text(
            text = stringResource(
                R.string.document_capture_method_subtitle,
                stringResource(documentType.titleRes)
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.content_padding_normal))
        ) {
            CaptureMethodCard(
                methodText = stringResource(R.string.document_capture_method_scan),
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.element_spacing_normal)),
                onCardClick = { onCaptureMethod(UploadMethod.AUTO) }
            )

            CaptureMethodCard(
                methodText = stringResource(R.string.document_capture_method_photo),
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.element_spacing_normal)),
                onCardClick = { onCaptureMethod(UploadMethod.MANUAL) }
            )

            if (allowUploads) {
                CaptureMethodCard(
                    methodText = stringResource(R.string.document_capture_method_upload),
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.element_spacing_normal)),
                    onCardClick = { onCaptureMethod(UploadMethod.UPLOAD) }
                )
            }
        }
    }
}

@Composable
internal fun CaptureMethodCard(methodText: String, modifier: Modifier = Modifier, onCardClick: () -> Unit = {}) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shape = MaterialTheme.shapes.medium
    ) {
        // Card content
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(all = dimensionResource(id = R.dimen.content_padding_normal)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = methodText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview
@Composable
fun DocumentCaptureScreenPreview() {
    IdentityTheme {
        DocumentSelectionView(IdentityDocumentType.PASSPORT, false)
    }
}