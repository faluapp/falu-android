package io.falu.identity.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import io.falu.core.utils.toThrowable
import io.falu.identity.ContractArgs
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.ui.theme.IdentityTheme
import software.tingle.api.ResourceResponse

@Composable
internal fun IdentityVerificationBaseScreen(
    contractArgs: ContractArgs,
    viewModel: IdentityVerificationViewModel,
    content: @Composable () -> Unit
) {
    val response by viewModel.verification.observeAsState()

    ObserveVerificationAndCompose(response, onError = {}) { verification ->
        IdentityVerificationHeader(
            contractArgs.workspaceLogo,
            verification.workspace,
            verification.live,
        ) {
            content()
        }
    }
}

@Composable
internal fun IdentityVerificationHeader(
    logoUri: Uri,
    workspace: WorkspaceInfo,
    live: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.content_padding_normal)),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
        ) {
            WelcomeImage(logoUri = logoUri)

            // Information Card
            Card(
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .padding(vertical = dimensionResource(id = R.dimen.content_padding_normal_2x))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.content_padding_normal))
                        .padding(vertical = dimensionResource(R.dimen.content_padding_normal))
                ) {
                    // Workspace Name
                    Text(
                        text = workspace.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Identity Verification Title
                    Text(
                        text = stringResource(id = R.string.identity_verification_title_identity_verification).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Divider
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    if (!live) {
                        SandboxView()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(R.dimen.element_spacing_normal))
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun SandboxView() {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .padding(top = dimensionResource(R.dimen.element_spacing_normal)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .background(Color(0xFFFFB100)) // Use Color in ARGB hex format
        )

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(dimensionResource(R.dimen.element_spacing_normal))) // Rounded corners
                .background(Color(0xFFFFB100)),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.element_spacing_normal))
                    .padding(vertical = dimensionResource(R.dimen.element_spacing_normal_quarter)),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.identity_verification_text_mode_sandbox),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .background(Color(0xFFFFB100))
        )
    }
}

@Composable
internal fun WelcomeImage(logoUri: Uri) {
    Box(
        modifier = Modifier
            .size(65.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, Color.LightGray, CircleShape)
            .shadow(elevation = 3.dp, shape = CircleShape)
            .zIndex(1f)
    ) {
        AsyncImage(
            model = logoUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
internal fun ObserveVerificationAndCompose(
    response: ResourceResponse<Verification>?,
    onError: (Throwable?) -> Unit,
    onSuccess: @Composable (Verification) -> Unit
) {
    if (response != null && response.successful() && response.resource != null) {
        onSuccess(response.resource!!)
    } else {
        onError(response?.toThrowable())
    }
}

@Preview
@Composable
fun IdentityBasePreview() {
    IdentityTheme {
        IdentityVerificationHeader(Uri.EMPTY, WorkspaceInfo(name = "Showcases", country = "US"), false, {})
    }
}