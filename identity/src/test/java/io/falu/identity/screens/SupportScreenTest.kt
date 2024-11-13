package io.falu.identity.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.ContractArgs
import io.falu.identity.R
import io.falu.identity.TestApplication
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.models.Support
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.viewModel.IdentityVerificationViewModel
import okhttp3.Headers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [Build.VERSION_CODES.Q])
internal class SupportScreenTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val verificationResponse = MutableLiveData<ResourceResponse<Verification>?>(null)

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )
        on { verification }.thenReturn(verificationResponse)
    }

    private val verification = mock<Verification>().also {
        whenever(it.support).thenReturn(
            Support(
                email = "support@example.com",
                phone = "+2547123456789",
                url = "https://support.example.com"
            )
        )
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test loading of support data and initiate calls and emails`() {
        setComposeTestRuleWith {
            onNodeWithText(context.getString(R.string.support_text_email)).assertExists().performClick()
            onNodeWithText(context.getString(R.string.support_text_call)).assertExists().performClick()
            onNodeWithText(verification.support?.url ?: "").assertExists()
        }
    }

    private fun setComposeTestRuleWith(testBlock: ComposeContentTestRule.() -> Unit = {}) {
        val response = ResourceResponse(
            200,
            Headers.headersOf(),
            verification,
            null
        )
        verificationResponse.postValue(response)

        composeTestRule.setContent {
            SupportScreen(mockIdentityVerificationViewModel)
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