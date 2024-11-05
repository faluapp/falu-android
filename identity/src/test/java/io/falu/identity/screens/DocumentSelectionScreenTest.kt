package io.falu.identity.screens

import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.ContractArgs
import io.falu.identity.viewModel.IdentityVerificationViewModel
import io.falu.identity.TestApplication
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.country.Country
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationOptions
import io.falu.identity.api.models.verification.VerificationOptionsForDocument
import io.falu.identity.api.models.verification.VerificationType
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.ui.TAG_INPUT_ISSUING_COUNTRY
import okhttp3.Headers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [Build.VERSION_CODES.Q])
internal class DocumentSelectionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val verificationResponse = MutableLiveData<ResourceResponse<Verification>?>(null)
    private val supportedCountriesResponse = MutableLiveData<ResourceResponse<Array<SupportedCountry>>?>()

    private val navActions = mock<IdentityVerificationNavActions> {
        on { navigateToDocumentCaptureMethods(any()) }.then {}
        on { navigateToError(any()) }.then {}
    }

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )
        on { verification }.thenReturn(verificationResponse)
        on { supportedCountries }.thenReturn(supportedCountriesResponse)
    }

    private val supportedCountries = arrayOf(
        SupportedCountry(
            country = Country("ken", "Kenya", flag = "http://cake.com/flag.svg"),
            documents = mutableListOf(
                IdentityDocumentType.IDENTITY_CARD,
                IdentityDocumentType.DRIVING_LICENSE,
                IdentityDocumentType.PASSPORT
            )
        )
    )

    private val verificationWithAllowedDocuments = mock<Verification>().also {
        whenever(it.type).thenReturn(VerificationType.DOCUMENT)
        whenever(it.options).thenReturn(
            VerificationOptions(
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
    fun `test setup of supported countries and available documents`() {
        val response = ResourceResponse(
            200,
            Headers.headersOf(),
            verificationWithAllowedDocuments,
            null
        )

        setComposeTestRuleWith(response) {
            onNodeWithTag(TAG_INPUT_ISSUING_COUNTRY).performClick()

            onNodeWithTag(TAG_DOCUMENT_ID_CARD).assertExists().assertIsNotEnabled()
            onNodeWithTag(TAG_DOCUMENT_PASSPORT).assertExists().assertIsNotEnabled()
            onNodeWithTag(TAG_DOCUMENT_DL).assertExists().assertIsNotEnabled()
        }
    }

    @Test
    fun `test if identity card document selected and continue`() {
        val response = ResourceResponse(
            200,
            Headers.headersOf(),
            verificationWithAllowedDocuments,
            null
        )

        setComposeTestRuleWith(response) {
            composeTestRule.onNodeWithTag(TAG_DOCUMENT_ID_CARD).performClick()

            composeTestRule.onNodeWithTag(TAG_CONTINUE_BUTTON).performClick()
            composeTestRule.onNodeWithTag(TAG_CONTINUE_BUTTON).assertIsNotEnabled()
        }
    }

    private fun setComposeTestRuleWith(
        response: ResourceResponse<Verification>,
        testBlock: ComposeContentTestRule.() -> Unit = {}
    ) {
        verificationResponse.postValue(response)
        supportedCountriesResponse.postValue(
            ResourceResponse(
                200, Headers.headersOf(), supportedCountries,
                null
            )
        )
        composeTestRule.setContent {
            DocumentSelectionScreen(mockIdentityVerificationViewModel, navActions)
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