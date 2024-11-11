package io.falu.identity.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.falu.core.utils.toThrowable
import io.falu.identity.R
import io.falu.identity.api.models.requirements.RequirementType
import io.falu.identity.api.models.requirements.RequirementType.Companion.nextDestination
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.ui.theme.IdentityTheme
import io.falu.identity.viewModel.FallbackUrlCallback
import io.falu.identity.viewModel.IdentityVerificationViewModel
import software.tingle.api.ResourceResponse

/**
 * Initial screen with a spinner, to decide which screen to navigate to based on pending [RequirementType].
 */
@Composable
internal fun InitialLoadingScreen(
    identityViewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    fallbackUrlCallback: FallbackUrlCallback
) {
    val verificationResponse by identityViewModel.verification.observeAsState()

    ObserveVerificationAndCompose(verificationResponse,
        onError = { throwable -> navActions.navigateToErrorWithFailure(throwable) }) { verification ->
        LaunchedEffect(Unit) {
            if (!verification.supported) {
                fallbackUrlCallback.launchFallbackUrl(verification.url.orEmpty())
            } else {
                verification.requirements.pending.nextDestination(navActions, verification)
            }
        }
    }
}

@Composable
internal fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(dimensionResource(R.dimen.content_padding_normal_2x)),
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 4.dp,
            trackColor = Color.LightGray,
        )
    }
}


@Composable
internal fun ObserveVerificationAndCompose(
    response: ResourceResponse<Verification>?,
    onError: (Throwable?) -> Unit,
    onSuccess: @Composable (Verification) -> Unit
) {
    when {
        response == null -> LoadingScreen()
        response.successful() && response.resource != null -> response.resource?.let { onSuccess(it) }
        else -> onError(response.toThrowable())
    }
}

@Preview
@Composable
internal fun LoadingScreenPreview() {
    IdentityTheme {
        LoadingScreen()
    }
}