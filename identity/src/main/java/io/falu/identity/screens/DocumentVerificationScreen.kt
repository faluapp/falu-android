package io.falu.identity.screens

import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_DOCUMENT_VERIFICATION
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.verification.Gender
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationIdNumberUpload
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.navigation.DocumentVerificationDestination
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.ui.IdentityVerificationHeader
import io.falu.identity.ui.LoadingButton
import io.falu.identity.ui.TextFieldError
import io.falu.identity.ui.theme.IdentityTheme
import io.falu.identity.viewModel.IdentityVerificationViewModel
import java.util.Date

@Composable
internal fun DocumentVerificationScreen(
    viewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions
) {
    val verificationResponse by viewModel.verification.observeAsState()
    var loading by remember { mutableStateOf(false) }

    ObserveVerificationAndCompose(verificationResponse, onError = {}) { verification ->
        LaunchedEffect(Unit) {
            viewModel.reportTelemetry(
                viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_DOCUMENT_VERIFICATION)
            )
        }

        DocumentVerificationForm(loading = loading) { idNumberUpload, isLoading ->
            loading = isLoading
            attemptSubmission(viewModel, navActions, idNumberUpload, verification, onLoading = { loading = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentVerificationForm(loading: Boolean, onSubmit: (VerificationIdNumberUpload, Boolean) -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var documentNumber by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf<Date?>(null) }
    val genderOptions = Gender.entries.map { context.getString(it.desc) }
    var expandedGenderMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var documentNumberError by remember { mutableStateOf(false) }
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var birthdayError by remember { mutableStateOf(false) }

    val formValid: () -> Boolean = {
        documentNumberError = documentNumber.isEmpty()
        firstNameError = firstName.isEmpty()
        lastNameError = lastName.isEmpty()
        genderError = gender.isEmpty()
        birthdayError = birthday == null
        !(documentNumberError || firstNameError || lastNameError || genderError || birthdayError)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(
                horizontal = dimensionResource(R.dimen.content_padding_normal),
                vertical = dimensionResource(R.dimen.element_spacing_normal)
            ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.element_spacing_normal_half))
    ) {
        Text(
            text = stringResource(R.string.document_verification_title_document_verification),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.element_spacing_normal))
        )

        OutlinedTextField(
            value = documentNumber,
            onValueChange = { documentNumber = it },
            maxLines = 1,
            label = {
                Text(
                    text = stringResource(R.string.document_verification_hint_document_number),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.element_spacing_normal_half)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
        if (documentNumberError) {
            TextFieldError(error = stringResource(R.string.document_verification_error_invalid_document_number))
        }

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            maxLines = 1,
            label = {
                Text(
                    text = stringResource(R.string.document_verification_hint_first_name),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.element_spacing_normal_half)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
        if (firstNameError) {
            TextFieldError(error = stringResource(R.string.document_verification_error_invalid_first_name))
        }

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            maxLines = 1,
            label = {
                Text(
                    text = stringResource(R.string.document_verification_hint_last_name),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.element_spacing_normal_half)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
        if (lastNameError) {
            TextFieldError(error = stringResource(R.string.document_verification_error_invalid_last_name))
        }

        ExposedDropdownMenuBox(
            expanded = expandedGenderMenu,
            onExpandedChange = { expandedGenderMenu = !expandedGenderMenu }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = {
                    Text(
                        text = stringResource(R.string.document_verification_hint_gender),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedGenderMenu = true },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expandedGenderMenu,
                        modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expandedGenderMenu,
                onDismissRequest = { expandedGenderMenu = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            expandedGenderMenu = false
                        }
                    )
                }
            }
        }
        if (genderError) {
            TextFieldError(error = stringResource(R.string.document_verification_error_invalid_first_name))
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_spacing_normal_half)))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.element_spacing_normal))
                )
                .clickable { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .padding(all = dimensionResource(R.dimen.content_padding_normal))
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (birthday == null) {
                        stringResource(R.string.document_verification_hint_birthday)
                    } else {
                        DateUtils.formatDateTime(
                            context,
                            birthday?.time ?: 0,
                            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
        if (birthdayError) {
            TextFieldError(error = stringResource(R.string.document_verification_hint_birthday))
        }

        if (showDatePicker) {
            DateOfBirthPicker(onDismiss = { showDatePicker = false }) { dateInMillis ->
                showDatePicker = false
                dateInMillis?.let { birthday = Date(it) }
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.element_spacing_normal_half)))

        LoadingButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.button_continue),
            isLoading = loading
        ) {
            if (formValid()) {
                onSubmit(attemptSubmission(documentNumber, firstName, lastName, birthday!!, gender), true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthPicker(onDismiss: () -> Unit, onSelected: (Long?) -> Unit) {
    val dateState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= System.currentTimeMillis()
        }
    )
    val millisToLocalDate = dateState.selectedDateMillis

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = { onSelected(millisToLocalDate) }
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    ) {
        DatePicker(
            state = dateState,
            showModeToggle = true
        )
    }
}

private fun attemptSubmission(
    documentNumber: String,
    firstName: String,
    lastName: String,
    dateOfBirth: Date,
    gender: String
) = VerificationIdNumberUpload(
    type = IdentityDocumentType.IDENTITY_CARD,
    number = documentNumber,
    firstName = firstName,
    lastName = lastName,
    birthday = dateOfBirth,
    sex = gender
)

private fun attemptSubmission(
    viewModel: IdentityVerificationViewModel,
    navActions: IdentityVerificationNavActions,
    idNumberUpload: VerificationIdNumberUpload,
    verification: Verification,
    onLoading: (Boolean) -> Unit = {}
) {
    val options = VerificationUpdateOptions(idNumber = idNumberUpload)
    val uploadRequest = VerificationUploadRequest(idNumber = idNumberUpload)

    viewModel.updateVerification(
        options,
        onSuccess = {
            onLoading(false)
            viewModel.attemptDocumentSubmission(
                DocumentVerificationDestination.ROUTE.route,
                navActions,
                verification,
                uploadRequest
            )
        },
        onError = { throwable ->
            onLoading(false)
            navActions.navigateToErrorWithApiExceptions(throwable)
        },
        onFailure = { throwable ->
            onLoading(false)
            navActions.navigateToErrorWithFailure(throwable)
        }
    )
}

@Preview
@Composable
private fun DocumentVerificationPreview() {
    IdentityTheme {
        IdentityVerificationHeader(Uri.EMPTY, WorkspaceInfo(name = "Showcases", country = "US"), false) {
            DocumentVerificationForm(loading = true) { _, _ -> }
        }
    }
}