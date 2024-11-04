package io.falu.identity.screens.error

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.falu.identity.R
import io.falu.identity.navigation.ErrorDestination
import io.falu.identity.ui.theme.IdentityTheme

@Composable
internal fun ErrorScreen(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    message: String? = null,
    primaryButton: ErrorScreenButton? = null,
    secondaryButton: ErrorScreenButton? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = dimensionResource(id = R.dimen.content_padding_normal))
    ) {
        Column(
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.content_padding_normal))
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing_normal)))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing_normal)))

            Text(
                text = message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.element_spacing_normal))) {
                primaryButton?.let {
                    Button(onClick = { primaryButton.onClick() }, modifier = modifier.fillMaxWidth()) {
                        Text(primaryButton.text)
                    }
                }

                secondaryButton?.let {
                    Button(onClick = { secondaryButton.onClick() }, modifier = modifier.fillMaxWidth()) {
                        Text(secondaryButton.text)
                    }
                }
            }
        }
    }
}

internal data class ErrorScreenButton(
    val text: String,
    val onClick: () -> Unit
)

@Preview
@Composable
fun ErrorScreenPreview() {
    val error = ErrorDestination(
        title = stringResource(R.string.error_title),
        desc = stringResource(R.string.error_title_unexpected_error),
        backButtonText = stringResource(R.string.button_rectify)
    )

    IdentityTheme {
        ErrorScreen(
            title = error.title,
            desc = error.desc,
            primaryButton = ErrorScreenButton(text = stringResource(R.string.button_rectify), onClick = {})
        )
    }
}