package io.falu.identity.api

import android.os.Build
import com.google.gson.Gson
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.country.Country
import io.falu.identity.api.models.country.SupportedCountry
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import software.tingle.api.ResourceResponse
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class FilesApiClientTests {

    private val gson = Gson()

    private lateinit var mockWebServer: MockWebServer

    private val apiClient: FilesApiClient = mock()

    private val supportedCountry = SupportedCountry(
        country = Country("ken", "Kenya", flag = "http://cake.com/flag.svg"),
        documents = mutableListOf(
            IdentityDocumentType.IDENTITY_CARD,
            IdentityDocumentType.PASSPORT,
            IdentityDocumentType.DRIVING_LICENSE
        )
    )

    @BeforeTest
    fun config() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun `test if fetching supported countries works`() {
        mockWebServer.url("$CDN_BASE_URL/identity/supported-countries.json")

        val resourceResponse = getResponse(tResponse = arrayOf(supportedCountry))
        whenever(apiClient.getSupportedCountries()).thenReturn(resourceResponse)

        mockWebServer.enqueue(getMockedResponse(tResponse = arrayOf(supportedCountry)))

        val response = apiClient.getSupportedCountries()
        assertNotNull(response.resource)
        assertEquals(response.resource!!.size, arrayOf(supportedCountry).size)
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
        private const val CDN_BASE_URL = "https://cdn.falu.io"
    }
}