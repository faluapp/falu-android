package io.falu.identity.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.falu.core.utils.toThrowable
import io.falu.identity.IdentityVerificationNavActions
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.api.models.requirements.RequirementType
import io.falu.identity.api.models.requirements.RequirementType.Companion.nextDestination
import io.falu.identity.api.models.verification.Verification
import software.tingle.api.ResourceResponse

/**
 * Initial screen with a spinner, to decide which screen to navigate to based on pending [RequirementType].
 */
@Composable
internal fun InitialLoadingScreen(
    identityViewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
) {
    val verificationResponse by identityViewModel.verification.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            verification.requirements.pending.nextDestination(navActions, verification)
        }
    }
}

@Composable
internal fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(bottom = 32.dp))
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
