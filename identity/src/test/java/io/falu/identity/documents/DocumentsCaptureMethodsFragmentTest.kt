package io.falu.identity.documents

import android.os.Build
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationOptions
import io.falu.identity.api.models.verification.VerificationOptionsForDocument
import io.falu.identity.databinding.FragmentDocumentCaptureMethodsBinding
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import com.google.android.material.R as MatR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class DocumentsCaptureMethodsFragmentTest {

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {}

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
                    ),
                )
            )
        )
    }

    private fun successfulVerification(data: Verification = verificationAllowUploads) {
        val successCaptor: KArgumentCaptor<(Verification) -> Unit> = argumentCaptor()
        verify(mockIdentityVerificationViewModel, times(1)).observeForVerificationResults(
            any(),
            successCaptor.capture(),
            any()
        )
        successCaptor.lastValue(data)
    }

    @Test
    fun `test when verification allows for uploads`() {
        launchFragment { binding, _ ->
            successfulVerification()
            assertEquals(binding.viewCaptureMethodUpload.visibility, View.VISIBLE)
        }
    }

    private fun launchFragment(block: (binding: FragmentDocumentCaptureMethodsBinding, navController: TestNavHostController) -> Unit) {
        launchFragmentInContainer(
            fragmentArgs = bundleOf(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE to IdentityDocumentType.IDENTITY_CARD),
            themeResId = MatR.style.Theme_MaterialComponents
        ) {
            DocumentCaptureMethodsFragment(createFactoryFor(mockIdentityVerificationViewModel))
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_document_capture_methods)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentDocumentCaptureMethodsBinding.bind(it.requireView()), navController)
        }
    }
}