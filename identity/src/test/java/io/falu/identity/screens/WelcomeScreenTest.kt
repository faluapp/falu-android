package io.falu.identity.screens

import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.ContractArgs
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.requirements.Requirement
import io.falu.identity.api.models.requirements.RequirementType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.navigation.ErrorDestination
import io.falu.identity.navigation.IdentityVerificationNavActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class WelcomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockVerificationResultCallback = mock<IdentityVerificationResultCallback>()
    private val navActions = mock<IdentityVerificationNavActions> {
        on { navigateToDocumentSelection() }.then {}
        on { navigateToError(any()) }.then {}
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
    }

    @Test
    fun `test if consent agreed to and update verification data`() {
        setComposeTestRuleWith {
            onNodeWithTag(WELCOME_ACCEPT_BUTTON).performClick()

            verify(mockIdentityVerificationViewModel).updateVerification(
                VerificationUpdateOptions(consent = true),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `test navigation to document selection on successful verification update`() {
        whenever(mockIdentityVerificationViewModel.updateVerification(any(), any(), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<(Unit) -> Unit>(1)
                onSuccess(Unit)
            }

        setComposeTestRuleWith {
            onNodeWithTag(WELCOME_ACCEPT_BUTTON).performClick()
            verify(navActions).navigateToDocumentSelection()
        }
    }

    @Test
    fun `test navigation to error screen on verification update failure`() {
        whenever(mockIdentityVerificationViewModel.updateVerification(any(), any(), any(), any()))
            .thenAnswer { invocation ->
                val onFailure = invocation.getArgument<(Throwable) -> Unit>(2)
                onFailure(Throwable("Mock failure"))
            }

        setComposeTestRuleWith {
            onNodeWithTag(WELCOME_ACCEPT_BUTTON).performClick()
            verify(navActions).navigateToError(
                ErrorDestination(title="", desc="", message="", backButtonText="")
            )
        }
    }
    private fun setComposeTestRuleWith(
        testBlock: ComposeContentTestRule.() -> Unit = {}
    ) {
        composeTestRule.setContent {
            WelcomeScreen(mockIdentityVerificationViewModel, navActions, mockVerificationResultCallback)
        }

        with(composeTestRule, testBlock)
    }

    private companion object {
        const val temporaryKey = "fskt_1234"
        val logo = mock<Uri>()

        const val WELCOME_ACCEPT_BUTTON = "Accept"

        val contractArgs = ContractArgs(
            temporaryKey = temporaryKey,
            verificationId = "iv_1234",
            maxNetworkRetries = 0,
            workspaceLogo = logo
        )
    }
}