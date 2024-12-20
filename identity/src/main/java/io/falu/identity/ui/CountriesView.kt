package io.falu.identity.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import io.falu.core.utils.toThrowable
import io.falu.identity.R
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.country.Country
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.ui.theme.IdentityTheme
import software.tingle.api.ResourceResponse

internal const val TAG_INPUT_ISSUING_COUNTRY = "input_issuing_country"

@Composable
internal fun CountriesView(
    response: ResourceResponse<Array<SupportedCountry>>?,
    selected: SupportedCountry?,
    onCountrySelected: (SupportedCountry) -> Unit = {}
) {
    ObserveCountriesAndCompose(response, onError = {}) { supportedCountries ->
        SupportedCountryViews(selected, supportedCountries, onCountrySelected)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportedCountryViews(
    selected: SupportedCountry?,
    supportedCountries: Array<SupportedCountry>,
    onCountrySelected: (SupportedCountry) -> Unit = {}
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
    ) {
        Text(text = stringResource(R.string.document_selection_subtitle), style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.element_spacing_normal)))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selected?.country?.name.orEmpty(),
                onValueChange = {},
                label = {
                    Text(
                        text = stringResource(R.string.document_selection_hint_issuing_country),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                        modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .semantics {
                        testTag = TAG_INPUT_ISSUING_COUNTRY
                    }
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                supportedCountries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(option.country.flag)
                                        .decoderFactory(SvgDecoder.Factory())
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = option.country.name,
                                    modifier = Modifier.padding(
                                        start = dimensionResource(R.dimen.element_spacing_normal)
                                    )
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onCountrySelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ObserveCountriesAndCompose(
    response: ResourceResponse<Array<SupportedCountry>>?,
    onError: (Throwable?) -> Unit,
    onSuccess: @Composable (Array<SupportedCountry>) -> Unit
) {
    if (response != null && response.successful() && response.resource != null) {
        onSuccess(response.resource!!)
    } else {
        onError(response?.toThrowable())
    }
}

@Preview
@Composable
fun WelcomePreview() {
    IdentityTheme {
        IdentityVerificationHeader(Uri.EMPTY, WorkspaceInfo(name = "Showcases", country = "Kenya"), false) {
            val country = SupportedCountry(
                Country("ke", "Kenya", "https://cdn.tinglesoftware.com/statics/countries/flags/svg/ken.svg"),
                documents = mutableListOf(IdentityDocumentType.PASSPORT)
            )

            SupportedCountryViews(
                selected = country,
                arrayOf(country)
            )
        }
    }
}