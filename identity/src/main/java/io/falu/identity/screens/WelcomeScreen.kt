package io.falu.identity.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_WELCOME
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.ui.IdentityVerificationHeader
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.ObserveVerificationAndCompose
import io.falu.identity.ui.theme.IdentityTheme
import io.falu.identity.viewModel.IdentityVerificationViewModel

internal const val WELCOME_ACCEPT_BUTTON = "Accept"

@Composable
internal fun WelcomeScreen(
    viewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    verificationResultCallback: IdentityVerificationResultCallback
) {
    val response by viewModel.verification.observeAsState()
    var isAcceptLoading by remember { mutableStateOf(false) }

    ObserveVerificationAndCompose(response, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_WELCOME)
            )
        }
        ConsentView(
            workspaceName = verification.workspace.name.replaceFirstChar { it.uppercase() },
            loading = isAcceptLoading,
            onAccepted = {
                isAcceptLoading = true
                viewModel.updateVerification(
                    VerificationUpdateOptions(consent = true),
                    onSuccess = {
                        isAcceptLoading = false
                        navActions.navigateToDocumentSelection()
                    },
                    onError = { throwable ->
                        isAcceptLoading = false
                        navActions.navigateToErrorWithApiExceptions(throwable)
                    },
                    onFailure = { throwable ->
                        navActions.navigateToErrorWithFailure(throwable)
                    }
                )
            },
            onDeclined = { verificationResultCallback.onFinishWithResult(IdentityVerificationResult.Canceled) }
        )
    }
}

@Composable
private fun ConsentView(
    workspaceName: String,
    loading: Boolean,
    onAccepted: () -> Unit,
    onDeclined: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.content_padding_normal))
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(R.string.welcome_subtitle, workspaceName.replaceFirstChar { it.uppercase() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.element_spacing_normal)),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = stringResource(id = R.string.welcome_body),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.element_spacing_normal)),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )

        LoadingButton(
            modifier = Modifier.semantics { testTag = WELCOME_ACCEPT_BUTTON },
            text = stringResource(R.string.welcome_button_accept),
            isLoading = loading
        ) {
            onAccepted()
        }

        LoadingButton(text = stringResource(R.string.welcome_button_decline)) { onDeclined() }
    }
}

@Preview
@Composable
private fun WelcomePreview() {
    IdentityTheme {
        IdentityVerificationHeader(Uri.EMPTY, WorkspaceInfo(name = "Showcases", country = "US"), false) {
            ConsentView(workspaceName = "Showcases", loading = false, onAccepted = {}, onDeclined = {})
        }
    }
}