package io.falu.identity.screens

import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.ContractArgs
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.TestApplication
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.requirements.Requirement
import io.falu.identity.api.models.requirements.RequirementType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.viewModel.IdentityVerificationViewModel
import okhttp3.Headers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [Build.VERSION_CODES.Q])
internal class WelcomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val verificationResponse = MutableLiveData<ResourceResponse<Verification>?>(null)
    private val mockVerificationResultCallback = mock<IdentityVerificationResultCallback>()
    private val navActions = mock<IdentityVerificationNavActions> {
        on { navigateToDocumentSelection() }.then {}
        on { navigateToErrorWithFailure(any()) }.then {}
    }

    private val verification = mock<Verification>().also {
        whenever(it.workspace).thenReturn(
            WorkspaceInfo("Test", "ken")
        )
        whenever(it.requirements).thenReturn(
            Requirement(pending = mutableListOf(RequirementType.CONSENT), errors = mutableListOf())
        )
        whenever(it.remainingAttempts).thenReturn(null)
    }

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { contractArgs }.thenReturn(contractArgs)

        on { apiClient }.thenReturn(
            IdentityVerificationApiClient(
                context = ApplicationProvider.getApplicationContext(),
                apiKey = temporaryKey,
                maxNetworkRetries = 0,
                enableLogging = true
            )
        )

        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )

        on { verification }.thenReturn(verificationResponse)
    }

    @Test
    fun `test if consent agreed to and update verification data`() {
        setComposeTestRuleWith {
            onNodeWithTag(WELCOME_ACCEPT_BUTTON).performClick()

            verify(mockIdentityVerificationViewModel).updateVerification(
                eq(VerificationUpdateOptions(consent = true)),
                any(),
                any(),
                any()
            )
        }
    }

    private fun setComposeTestRuleWith(
        testBlock: ComposeContentTestRule.() -> Unit = {}
    ) {
        val response = ResourceResponse(
            200,
            Headers.headersOf(),
            verification,
            null
        )
        verificationResponse.postValue(response)

        composeTestRule.setContent {
            WelcomeScreen(mockIdentityVerificationViewModel, navActions, mockVerificationResultCallback)
        }

        with(composeTestRule, testBlock)
    }

    private companion object {
        const val temporaryKey = "fskt_1234"
        val logo = mock<Uri>()

        val contractArgs = ContractArgs(
            temporaryKey = temporaryKey,
            verificationId = "iv_1234",
            maxNetworkRetries = 0,
            workspaceLogo = logo
        )
    }
}