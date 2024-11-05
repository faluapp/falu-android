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
import io.falu.identity.viewModel.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.TestApplication
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationOptions
import io.falu.identity.api.models.verification.VerificationOptionsForDocument
import okhttp3.Headers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [Build.VERSION_CODES.Q])
internal class DocumentCaptureMethodScreenTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val verificationResponse = MutableLiveData<ResourceResponse<Verification>?>(null)
    private val mockNavigateToCaptureMethod: (UploadMethod) -> Unit = mock()

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )
        on { verification }.thenReturn(verificationResponse)
    }

    private val verificationAllowUploads = mock<Verification>().also {
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
    fun `test capture method card navigation`() {
        setComposeTestRuleWith {
            onNodeWithText(context.getString(R.string.document_capture_method_scan)).assertExists().performClick()
            verify(mockNavigateToCaptureMethod).invoke(UploadMethod.AUTO)

            onNodeWithText(context.getString(R.string.document_capture_method_photo)).assertExists().performClick()
            verify(mockNavigateToCaptureMethod).invoke(UploadMethod.MANUAL)

            onNodeWithText(context.getString(R.string.document_capture_method_upload)).assertExists().performClick()
            verify(mockNavigateToCaptureMethod).invoke(UploadMethod.UPLOAD)
        }
    }

    private fun setComposeTestRuleWith(
        documentType: IdentityDocumentType = IdentityDocumentType.PASSPORT,
        testBlock: ComposeContentTestRule.() -> Unit = {}
    ) {
        val response = ResourceResponse(
            200,
            Headers.headersOf(),
            verificationAllowUploads,
            null
        )
        verificationResponse.postValue(response)

        composeTestRule.setContent {
            DocumentCaptureMethodsScreen(mockIdentityVerificationViewModel, documentType, mockNavigateToCaptureMethod)
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