package io.falu.identity.documents

import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.button.MaterialButton
import io.falu.identity.ContractArgs
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.country.Country
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationOptions
import io.falu.identity.api.models.verification.VerificationOptionsForDocument
import io.falu.identity.api.models.verification.VerificationType
import io.falu.identity.databinding.FragmentDocumentSelectionBinding
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import com.google.android.material.R as MatR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class DocumentSelectionFragmentTest {

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )
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

    private fun successfulVerification(data: Verification = verificationWithAllowedDocuments) {
        val successCaptor: KArgumentCaptor<(Verification) -> Unit> = argumentCaptor()
        verify(mockIdentityVerificationViewModel, times(1)).observeForVerificationResults(
            any(),
            successCaptor.capture(),
            any()
        )
        successCaptor.lastValue(data)
    }

    private fun getSupportedCountries() {
        val successCaptor: KArgumentCaptor<(Array<SupportedCountry>) -> Unit> = argumentCaptor()
        verify(mockIdentityVerificationViewModel)
            .observerForSupportedCountriesResults(any(), successCaptor.capture(), any())

        successCaptor.lastValue(supportedCountries)
    }

    @Test
    fun `test setup of supported countries and available documents`() {
        launchDocumentSelectionFragment { binding, _ ->
            getSupportedCountries()

            successfulVerification()

            assertEquals(binding.inputIssuingCountry.text.toString(), "Kenya")
            assertEquals(binding.chipDrivingLicense.isEnabled, true)
            assertEquals(binding.chipIdentityCard.isEnabled, true)
            assertEquals(binding.chipPassport.isEnabled, true)
        }
    }

    @Test
    fun `test if identity card document selected and continue`() {
        launchDocumentSelectionFragment { binding, _ ->
            getSupportedCountries()

            successfulVerification()

            binding.chipIdentityCard.isChecked = true

            binding.buttonContinue.findViewById<MaterialButton>(R.id.button_loading).callOnClick()

            verify(mockIdentityVerificationViewModel).updateVerification(any(), any(), any(), any())

            assertEquals(
                binding.buttonContinue.findViewById<MaterialButton>(R.id.button_loading).isEnabled,
                false
            )
            assertEquals(
                binding.buttonContinue.findViewById<ProgressBar>(R.id.progress_view).visibility,
                View.VISIBLE
            )
        }
    }

    private fun launchDocumentSelectionFragment(
        block: (binding: FragmentDocumentSelectionBinding, navController: TestNavHostController) -> Unit
    ) {
        launchFragmentInContainer(themeResId = MatR.style.Theme_MaterialComponents) {
            DocumentSelectionFragment(createFactoryFor(mockIdentityVerificationViewModel))
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_document_selection)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentDocumentSelectionBinding.bind(it.requireView()), navController)
        }
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