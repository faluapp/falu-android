package io.falu.identity.api

import android.os.Build
import com.google.gson.Gson
import io.falu.core.models.FaluFile
import io.falu.identity.api.models.*
import io.falu.identity.api.models.requirements.Requirement
import io.falu.identity.api.models.verification.*
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
import java.util.*
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
                documentType = DocumentDetector("", 80)
            )
        )
    )

    private val faluFile = FaluFile(
        id = "file_123",
        created = Date(),
        updated = Date(),
        purpose = filePurpose,
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
    companion object {        private const val baseUrl = "https://api.falu.io"        private const val filePurpose = "identity.private"    }}