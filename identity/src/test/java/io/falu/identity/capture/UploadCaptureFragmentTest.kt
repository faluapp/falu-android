package io.falu.identity.capture

import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.capture.upload.UploadCaptureFragment
import io.falu.identity.databinding.FragmentUploadCaptureBinding
import io.falu.identity.documents.DocumentSelectionFragment
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class UploadCaptureFragmentTest {

    private val uri = mock<Uri>()

    private val documentUploadDisposition = MutableLiveData(DocumentUploadDisposition())

    private val mockCaptureDocumentViewModel = mock<CaptureDocumentViewModel> {}

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        whenever(it.documentUploadDisposition).thenReturn(documentUploadDisposition)
    }

    @Test
    fun `test if result callbacks are initialized and UI correctness for ID cards and DLs`() {
        launchUploadFragment { binding, _, fragment ->
            val callbackCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()

            verify(mockCaptureDocumentViewModel).pickDocumentImages(
                same(fragment),
                callbackCaptor.capture(),
                callbackCaptor.capture()
            )

            assertEquals(binding.cardDocumentBack.visibility, View.VISIBLE)
            assertEquals(binding.progressSelectFront.visibility, View.GONE)
            assertEquals(binding.progressSelectBack.visibility, View.GONE)
            assertEquals(binding.buttonContinue.isEnabled, false)
        }
    }

    @Test
    fun `test if result callbacks are initialized and UI correctness for passports`() {
        launchUploadFragment(IdentityDocumentType.PASSPORT) { binding, _, fragment ->
            val callbackCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()

            verify(mockCaptureDocumentViewModel).pickDocumentImages(
                same(fragment),
                callbackCaptor.capture(),
                callbackCaptor.capture()
            )

            assertEquals(binding.cardDocumentBack.visibility, View.GONE)
            assertEquals(binding.progressSelectFront.visibility, View.GONE)
            assertEquals(binding.progressSelectBack.visibility, View.GONE)
            assertEquals(binding.buttonContinue.isEnabled, false)
        }
    }

    @Test
    fun `test if image pick works for id and dl`() {
        pickImages()
    }

    @Test
    fun `test if image pick works for passport`() {
        pickImages(IdentityDocumentType.PASSPORT)
    }

    private fun pickImages(documentType: IdentityDocumentType = IdentityDocumentType.IDENTITY_CARD) {
        launchUploadFragment { binding, _, fragment ->
            val frontImageCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()
            val backImageImageCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()

            verify(mockCaptureDocumentViewModel).pickDocumentImages(
                same(fragment),
                frontImageCaptor.capture(),
                backImageImageCaptor.capture()
            )

            binding.buttonSelectFront.callOnClick()

            verify(mockCaptureDocumentViewModel).pickImageFront()
            frontImageCaptor.lastValue(uri)

            binding.buttonSelectBack.callOnClick()

            if (documentType != IdentityDocumentType.PASSPORT) {
                verify(mockCaptureDocumentViewModel).pickImageFront()
                backImageImageCaptor.lastValue(uri)
            }
        }
    }

    private fun launchUploadFragment(
        documentType: IdentityDocumentType = IdentityDocumentType.IDENTITY_CARD,
        block: (binding: FragmentUploadCaptureBinding, navController: TestNavHostController, fragment: AbstractCaptureFragment) -> Unit
    ) {
        launchFragmentInContainer(
            bundleOf(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE to documentType),
            themeResId = R.style.Theme_MaterialComponents
        ) {
            UploadCaptureFragment(createFactoryFor(mockIdentityVerificationViewModel)).also {
                it.captureDocumentViewModelFactory = createFactoryFor(mockCaptureDocumentViewModel)
            }
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_upload_capture)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentUploadCaptureBinding.bind(it.requireView()), navController, it)
        }
    }

}