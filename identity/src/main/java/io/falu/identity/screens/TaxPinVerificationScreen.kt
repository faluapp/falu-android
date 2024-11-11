package io.falu.identity.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_TAX_PIN_VERIFICATION
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationTaxPinUpload
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.navigation.TaxPinDestination
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.TextFieldError
import io.falu.identity.viewModel.IdentityVerificationViewModel

@Composable
internal fun TaxPinVerificationScreen(
    viewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions
) {
    val verificationResponse by viewModel.verification.observeAsState()
    var loading by remember { mutableStateOf(false) }

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_TAX_PIN_VERIFICATION)
            )
        }

        TaxPinVerificationForm(loading) { pinOptions, isLoading ->
            loading = isLoading
            attemptSubmission(viewModel, navActions, pinOptions, verification, onLoading = { loading = it })
        }
    }
}

@Composable
private fun TaxPinVerificationForm(loading: Boolean, onSubmit: (VerificationTaxPinUpload, Boolean) -> Unit) {
    val scrollState = rememberScrollState()

    var pinNumber by remember { mutableStateOf("") }
    var pinNumberError by remember { mutableStateOf(false) }

    val formValid: () -> Boolean = {
        pinNumberError = pinNumber.isEmpty()
        !(pinNumberError)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(
                horizontal = dimensionResource(R.dimen.content_padding_normal),
                vertical = dimensionResource(R.dimen.element_spacing_normal)
            ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_spacing_normal_half))
    ) {
        Text(
            text = stringResource(R.string.tax_pin_verification_title_tax_pin),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.element_spacing_normal))
        )

        OutlinedTextField(
            value = pinNumber,
            onValueChange = { pinNumber = it },
            label = {
                Text(
                    text = stringResource(R.string.tax_pin_verification_hint_tax_pin),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.element_spacing_normal_half)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
        if (pinNumberError) {
            TextFieldError(error = stringResource(R.string.document_verification_error_invalid_document_number))
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_spacing_normal_half)))

        LoadingButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.button_continue),
            isLoading = loading
        ) {
            if (formValid()) {
                onSubmit(attemptSubmission(taxPin = pinNumber), true)
            }
        }
    }
}

private fun attemptSubmission(taxPin: String) = VerificationTaxPinUpload(value = taxPin)

private fun attemptSubmission(
    viewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    pinUploadOptions: VerificationTaxPinUpload,
    verification: Verification,
    onLoading: (Boolean) -> Unit = {},
) {
    val options = VerificationUpdateOptions(taxPin = pinUploadOptions)
    val uploadRequest = VerificationUploadRequest(taxPin = pinUploadOptions)

    viewModel.updateVerification(options,
        onSuccess = {
            onLoading(false)
            viewModel.attemptDocumentSubmission(
                TaxPinDestination.ROUTE.route,
                navActions,
                verification,
                uploadRequest
            )
        },
        onError = { throwable ->
            onLoading(false)
            navActions.navigateToErrorWithApiExceptions(throwable)
        },
        onFailure = { throwable ->
            onLoading(false)
            navActions.navigateToErrorWithFailure(throwable)
        }
    )
}