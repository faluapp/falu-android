package io.falu.identity.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_CONFIRMATION
import io.falu.identity.ui.ObserveVerificationAndCompose

@Composable
internal fun ConfirmationScreen(
    viewModel: IdentityVerificationViewModel,
    callback: IdentityVerificationResultCallback
) {
    val verificationResponse by viewModel.verification.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) {
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_CONFIRMATION)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(R.dimen.content_padding_normal))
        ) {
            Text(
                text = stringResource(R.string.confirmation_title_verification_processing),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.content_padding_normal)),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.confirmation_text_gratitude),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.element_spacing_normal)),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    viewModel.reportSuccessfulVerificationTelemetry()
                    callback.onFinishWithResult(IdentityVerificationResult.Succeeded)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.button_finish))
            }
        }
    }
}