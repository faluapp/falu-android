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
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.R
import io.falu.identity.TestApplication
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationOptions
import io.falu.identity.api.models.verification.VerificationOptionsForDocument
import io.falu.identity.viewModel.IdentityVerificationViewModel
import okhttp3.Headers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [Build.VERSION_CODES.Q])
internal class ConfirmationScreenTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mockVerificationResultCallback = mock<IdentityVerificationResultCallback>()
    private val verificationResponse = MutableLiveData<ResourceResponse<Verification>?>(null)

    @get:Rule
    val composeTestRule = createComposeRule()

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
        whenever(it.options).thenReturn(
            VerificationOptions(
                allowUploads = true,
                countries = mutableListOf("ken", "tza", "uga"),
                document = VerificationOptionsForDocument(

                    allowed = mutableListOf(
                        IdentityDocumentType.PASSPORT,
                        IdentityDocumentType.IDENTITY_CARD,
                        IdentityDocumentType.DRIVING_LICENSE
                    )
                )
            )
        )
    }

    @Test
    fun `test Button Click Finishes With Complete`() {
        setComposeTestRuleWith {
            onNodeWithText(context.getString(R.string.button_finish)).performClick()

            verify(mockVerificationResultCallback).onFinishWithResult(eq(IdentityVerificationResult.Succeeded))
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
            ConfirmationScreen(mockIdentityVerificationViewModel, mockVerificationResultCallback)
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