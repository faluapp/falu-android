package io.falu.identity.api

import android.os.Build
import com.google.gson.Gson
import io.falu.core.models.FaluFile
import io.falu.identity.api.models.Branding
import io.falu.identity.api.models.BusinessInfo
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.Support
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.requirements.Requirement
import io.falu.identity.api.models.verification.DocumentDetector
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.api.models.verification.VerificationDocumentSide
import io.falu.identity.api.models.verification.VerificationDocumentUpload
import io.falu.identity.api.models.verification.VerificationModel
import io.falu.identity.api.models.verification.VerificationOptions
import io.falu.identity.api.models.verification.VerificationOptionsForDocument
import io.falu.identity.api.models.verification.VerificationStatus
import io.falu.identity.api.models.verification.VerificationType
import io.falu.identity.api.models.verification.VerificationUploadRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse
import software.tingle.api.patch.JsonPatchDocument
import java.io.File
import java.util.Date
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class IdentityVerificationApiClientTests {

    private val gson = Gson()

    private val file = mock<File>()

    private lateinit var mockWebServer: MockWebServer

    private val apiClient: IdentityVerificationApiClient = mock()

    private val verification = Verification(
        id = "iv_1234",
        type = VerificationType.IDENTITY_NUMBER,
        status = VerificationStatus.INPUT_REQUIRED,
        options = VerificationOptions(
            countries = mutableListOf("ken"), document = VerificationOptionsForDocument(
                mutableListOf(IdentityDocumentType.IDENTITY_CARD, IdentityDocumentType.PASSPORT)
            )
        ),
        url = "https://example.com/verify",
        live = true,
        workspace = WorkspaceInfo("Test ", "ken"),
        business = BusinessInfo("Test", "www.test.com", "www.test.com/privacy", "www.test.com/tos"),
        support = Support("support@test.com", "+2547123456789", "https://support.test.com"),
        branding = Branding("", ""),
        requirements = Requirement(mutableListOf(), mutableListOf()),
        capture = VerificationCapture(
            timeout = 1000,
            blur = null,
            models = VerificationModel(
                document = DocumentDetector("", 80),
                face = DocumentDetector("", 80)
            )
        )
    )

    private val faluFile = FaluFile(
        id = "file_123",
        created = Date(),
        updated = Date(),
        purpose = FILE_PURPOSE,
        type = "",
        fileName = "iv_123_front.jpg",
        null,
        null
    )

    @BeforeTest
    fun config() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun `test if fetching verification works`() {
        mockWebServer.url("$BASE_URL/v1/identity/verifications/${verification.id}/workflow")

        val resourceResponse = getResponse(tResponse = verification)
        whenever(apiClient.getVerification(verification.id)).thenReturn(resourceResponse)

        mockWebServer.enqueue(getMockedResponse(tResponse = verification))

        val response = apiClient.getVerification(verification.id)
        assertNotNull(response.resource)
        assertEquals(response.resource!!.id, verification.id)
    }

    @Test
    fun `test if updating verification works`() {
        mockWebServer.url("$BASE_URL/v1/identity/verifications/${verification.id}/workflow")

        val document = JsonPatchDocument().replace("/test", "test")

        val resourceResponse = getResponse(tResponse = verification)
        whenever(apiClient.updateVerification(verification.id, document))
            .thenReturn(resourceResponse)

        mockWebServer.enqueue(getMockedResponse(tResponse = verification))

        val response = apiClient.updateVerification(verification.id, document)
        assertNotNull(response.resource)
        assertEquals(response.resource!!.id, verification.id)
    }

    @Test
    fun `test if identity document upload works`() {
        mockWebServer.url("$BASE_URL/v1/files")

        val resourceResponse = getResponse(tResponse = faluFile)
        whenever(apiClient.uploadIdentityDocuments(eq(verification.id), eq(FILE_PURPOSE), eq(file)))
            .thenReturn(resourceResponse)

        mockWebServer.enqueue(getMockedResponse(tResponse = faluFile))

        val response = apiClient.uploadIdentityDocuments(verification.id, FILE_PURPOSE, file)
        assertNotNull(response.resource)
        assertEquals(response.resource!!.id, faluFile.id)
    }

    @Test
    fun `test  if verification submission works`() {
        mockWebServer.url("$BASE_URL/v1/identity/verifications/$verification/workflow/submit")

        val request = VerificationUploadRequest(
            consent = true,
            country = "ken",
            document = VerificationDocumentUpload(
                IdentityDocumentType.IDENTITY_CARD,
                back = VerificationDocumentSide(file = "file_123", method = UploadMethod.MANUAL),
                front = VerificationDocumentSide(file = "file_456", method = UploadMethod.MANUAL)
            )
        )

        val resourceResponse = getResponse(tResponse = verification)

        whenever(apiClient.submitVerificationDocuments(eq(verification.id), eq(request)))
            .thenReturn(resourceResponse)

        mockWebServer.enqueue(getMockedResponse(tResponse = verification))

        val response = apiClient.submitVerificationDocuments(verification.id, request)
        assertNotNull(response.resource)
        assertEquals(response.resource!!.id, verification.id)
    }

    private fun <T> getMockedResponse(statusCode: Int = 200, tResponse: T?): MockResponse {
        val responseBody = gson.toJson(tResponse)

        return MockResponse()
            .setResponseCode(statusCode)
            .setBody(responseBody)
    }

    private fun <T> getResponse(statusCode: Int = 200, tResponse: T?): ResourceResponse<T> {
        val mockedResponse = getMockedResponse(statusCode, tResponse)
        return ResourceResponse(statusCode, mockedResponse.headers, tResponse, null)
    }

    companion object {
        private const val BASE_URL = "https://api.falu.io"
        private const val FILE_PURPOSE = "identity.private"
    }
}