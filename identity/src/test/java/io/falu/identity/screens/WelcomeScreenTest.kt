package io.falu.identity.screens

import android.net.Uri
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
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
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WelcomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockVerificationResultCallback = mock<IdentityVerificationResultCallback>()

    private val verification = mock<Verification>().also {
        whenever(it.workspace).thenReturn(
            WorkspaceInfo("Test", "ken")
        )
        whenever(it.requirements).thenReturn(
            Requirement(pending = mutableListOf(RequirementType.CONSENT), errors = mutableListOf())
        )
        whenever(it.remainingAttempts).thenReturn(null)
    }

    @Test
    fun `test if consent agreed to and update verification data`() {
        setComposeTestRuleWith {
        }
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

    private fun setComposeTestRuleWith(
        testBlock: ComposeContentTestRule.() -> Unit = {}
    ) {
        composeTestRule.setContent {
            WelcomeScreen(mockIdentityVerificationViewModel, {}, {})
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