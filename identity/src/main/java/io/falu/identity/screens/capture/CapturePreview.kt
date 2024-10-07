package io.falu.identity.screens.capture

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.falu.identity.R
import io.falu.identity.ui.theme.IdentityTheme

@Composable
internal fun CapturePreview(bitmap: Bitmap, onContinue: () -> Unit, onDiscard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(dimensionResource(R.dimen.element_spacing_normal))
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(240.dp)
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_spacing_normal_half)))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
        ) {
            Text(text = stringResource(id = R.string.button_continue))
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_spacing_normal_half)))

        Button(
            onClick = onDiscard,
            colors = buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.content_padding_normal))
        ) {
            Text(text = stringResource(id = R.string.button_scan_again))
        }
    }
}

@Preview
@Composable
fun CapturePreviewPreview() {
    IdentityTheme {
        CapturePreview(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888), {}, {})
    }
}