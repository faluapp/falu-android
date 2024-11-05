package io.falu.identity.screens

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.falu.identity.viewModel.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_SUPPORT

@Composable
internal fun SupportScreen(identityViewModel: IdentityVerificationViewModel) {
    val context = LocalContext.current
    val verificationResponse by identityViewModel.verification.observeAsState()

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            identityViewModel.reportTelemetry(
                identityViewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_SUPPORT)
            )
        }
        val support = verification.support

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                text = stringResource(R.string.support_title_help_needed),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.content_padding_normal)),
                textAlign = TextAlign.Center
            )

            SupportOption(
                iconRes = R.drawable.ic_call,
                label = stringResource(R.string.support_text_call),
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${support?.phone}")
                    }
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                thickness = dimensionResource(R.dimen.element_spacing_normal_half),
                modifier = Modifier.padding(top = dimensionResource(R.dimen.element_spacing_normal_half))
            )

            SupportOption(
                iconRes = R.drawable.ic_falu_email,
                label = stringResource(R.string.support_text_email),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(support?.email))
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                thickness = dimensionResource(R.dimen.element_spacing_normal_half),
                modifier = Modifier.padding(top = dimensionResource(R.dimen.element_spacing_normal_half))
            )

            Text(
                text = support?.url ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.element_spacing_normal)),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SupportOption(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(dimensionResource(R.dimen.element_spacing_normal))
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(dimensionResource(R.dimen.content_padding_normal))
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.element_spacing_normal_half))
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}