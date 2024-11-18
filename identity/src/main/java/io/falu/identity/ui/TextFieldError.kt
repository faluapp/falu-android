package io.falu.identity.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import io.falu.identity.R

@Composable
internal fun TextFieldError(error: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.content_padding_normal))
                .padding(top = dimensionResource(id = R.dimen.element_spacing_normal)),
            color = MaterialTheme.colorScheme.error
        )
    }
}