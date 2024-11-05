package io.falu.identity.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
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
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationType
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.screens.capture.ManualCaptureScreen
import io.falu.identity.utils.IdentityImageHandler
import okhttp3.Headers
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse
import java.io.File
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [Build.VERSION_CODES.Q])
internal class ManualCaptureScreenTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val documentUploadDisposition = MutableLiveData(DocumentUploadDisposition())
    private val modelFile = MutableLiveData<File>()

    private val verificationResponse = MutableLiveData<ResourceResponse<Verification>?>(null)
    private val mockImageHandler = mock<IdentityImageHandler>()

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        whenever(it.documentUploadDisposition).doReturn(documentUploadDisposition)
        on { it.documentDetectorModelFile } doReturn (modelFile)
        on { analyticsRequestBuilder }.doReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )
        on { verification }.doReturn(verificationResponse)
        on { imageHandler }.thenReturn(mockImageHandler)
    }

    private val verification = mock<Verification>().also {
        whenever(it.type).thenReturn(VerificationType.DOCUMENT)
    }

    private val navActions = mock<IdentityVerificationNavActions> {
        on { navigateToDocumentCaptureMethods(any()) }.then {}
        on { navigateToError(any()) }.then {}
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test if result callbacks are initialized and UI correctness for ID cards and DLs`() {
        setComposeTestRuleWith(documentType = IdentityDocumentType.IDENTITY_CARD) {
            val document = context.getString(IdentityDocumentType.IDENTITY_CARD.titleRes)
            val title = context.getString(R.string.upload_document_capture_title, document)

            // Check if the title is displayed correctly
            onNodeWithText(title).assertIsDisplayed()

            // Check that both front and back capture buttons are visible
            onNodeWithText(
                context.getString(
                    R.string.upload_document_capture_document_front,
                    document
                )
            ).assertIsDisplayed()
            onNodeWithText(
                context.getString(
                    R.string.upload_document_capture_document_back,
                    document
                )
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `test if result callbacks are initialized and UI correctness for passports`() {
        setComposeTestRuleWith(documentType = IdentityDocumentType.PASSPORT) {
            // Check if the title is displayed correctly for passport
            val document = context.getString(IdentityDocumentType.PASSPORT.titleRes)
            val title = context.getString(R.string.upload_document_capture_title, document)

            // Check if the title is displayed correctly
            onNodeWithText(title).assertIsDisplayed()

            // Check that both front and back capture buttons are visible
            onNodeWithText(
                context.getString(
                    R.string.upload_document_capture_document_front,
                    document
                )
            ).assertIsDisplayed()
            onNodeWithText(
                context.getString(
                    R.string.upload_document_capture_document_back,
                    document
                )
            ).assertDoesNotExist()
        }
    }

    @Test
    fun `test if image capture works for id and dl`() {
        setComposeTestRuleWith(documentType = IdentityDocumentType.IDENTITY_CARD) {
            val document = context.getString(IdentityDocumentType.IDENTITY_CARD.titleRes)

            // Simulate clicking on front capture button
            onNodeWithText(context.getString(R.string.button_select_front, document)).performClick()
            verify(mockIdentityVerificationViewModel.imageHandler).captureImageFront(any())

            // Simulate clicking on back capture button
            onNodeWithText(context.getString(R.string.button_select_back)).performClick()
            verify(mockIdentityVerificationViewModel.imageHandler).captureImageBack(any())
        }
    }

    @Test
    fun `test if image capture works for passport`() {
        setComposeTestRuleWith(documentType = IdentityDocumentType.PASSPORT) {
            // Simulate clicking on front capture button for passport
            onNodeWithText(context.getString(R.string.button_select_front)).performClick()
            verify(mockIdentityVerificationViewModel.imageHandler).captureImageFront(any())

            // Ensure back capture is not present
            onNodeWithText(context.getString(R.string.button_select_back)).assertDoesNotExist()
        }
    }

    private fun setComposeTestRuleWith(
        documentType: IdentityDocumentType,
        testBlock: ComposeContentTestRule.() -> Unit = {}
    ) {

        val response = ResourceResponse(
            200,
            Headers.headersOf(),
            verification,
            null
        )
        verificationResponse.postValue(response)

        documentUploadDisposition.postValue(DocumentUploadDisposition())

        composeTestRule.setContent {
            ManualCaptureScreen(mockIdentityVerificationViewModel, navActions, documentType)
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