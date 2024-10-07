package io.falu.identity.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_DOCUMENT_SELECTION
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.country.Country
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.ui.CountriesView
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.ObserveVerificationAndCompose
import io.falu.identity.ui.theme.IdentityTheme
import software.tingle.api.ResourceResponse

@Composable
internal fun DocumentSelectionScreen(
    viewModel: IdentityVerificationViewModel,
    navigateToCaptureMethods: (IdentityDocumentType) -> Unit,
    navigateToError: (Throwable) -> Unit
) {
    val verificationResponse by viewModel.verification.observeAsState()
    val supportedCountriesResponse by viewModel.supportedCountries.observeAsState()
    var selectedDocumentType by remember { mutableStateOf<IdentityDocumentType?>(null) }
    var selectedCountry by remember { mutableStateOf<SupportedCountry?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_DOCUMENT_SELECTION)
            )
        }

        Column {
            DocumentSelectionView(
                response = supportedCountriesResponse,
                verification.options.document.allowed,
                onSelected = { documentType, country ->
                    selectedDocumentType = documentType
                    selectedCountry = country
                }
            )

            Box(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.content_padding_normal))) {
                LoadingButton(text = stringResource(R.string.button_continue), enabled = selectedDocumentType != null) {
                    isLoading = true

                    val updateOptions = VerificationUpdateOptions(country = selectedCountry?.country?.code.orEmpty())

                    viewModel.updateVerification(
                        updateOptions,
                        onSuccess = { navigateToCaptureMethods(selectedDocumentType!!) },
                        onError = {},
                        onFailure = { navigateToError(it) }
                    )

                    isLoading = false
                }
            }
        }
    }
}

@Composable
private fun DocumentSelectionView(
    response: ResourceResponse<Array<SupportedCountry>>?,
    allowedDocuments: List<IdentityDocumentType>,
    onSelected: (IdentityDocumentType, SupportedCountry) -> Unit
) {
    var selectedCountry by remember { mutableStateOf<SupportedCountry?>(null) }

    Column {
        CountriesView(response, selectedCountry, onCountrySelected = {
            selectedCountry = it
        })
        DocumentOptions(allowedDocuments, selectedCountry, onSelected)
    }
}

@Composable
private fun DocumentOptions(
    allowedDocuments: List<IdentityDocumentType>,
    supportedCountry: SupportedCountry?,
    onSelected: (IdentityDocumentType, SupportedCountry) -> Unit
) {
    var selected by remember { mutableStateOf<IdentityDocumentType?>(null) }

    fun isSelected(documentType: IdentityDocumentType) = selected == documentType

    fun isEnabled(documentType: IdentityDocumentType) = supportedCountry?.documents?.contains(documentType) ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
    ) {

        Text(
            text = stringResource(id = R.string.document_selection_accepted_documents),
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.content_padding_normal))
                .padding(bottom = dimensionResource(R.dimen.element_spacing_normal))
        )

        allowedDocuments.forEach { documentType ->
            ElevatedFilterChip(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.element_spacing_normal)),
                selected = isSelected(documentType),
                enabled = isEnabled(documentType),
                onClick = {
                    selected = documentType
                    onSelected(documentType, supportedCountry!!)
                },
                label = {
                    Text(
                        text = stringResource(documentType.titleRes),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.content_padding_normal))
                    )
                },
                trailingIcon = {
                    if (isSelected(documentType)) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)

                        )
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun DocumentSelectionScreenPreview() {
    val country = SupportedCountry(
        Country("us", "US", "https://cdn.tinglesoftware.com/statics/countries/flags/svg/ken.svg"),
        documents = mutableListOf(IdentityDocumentType.PASSPORT)
    )

    IdentityTheme {
        DocumentOptions(
            listOf(
                IdentityDocumentType.PASSPORT,
                IdentityDocumentType.IDENTITY_CARD,
                IdentityDocumentType.DRIVING_LICENSE
            ),
            country
        ) { _, _ -> }
    }
}