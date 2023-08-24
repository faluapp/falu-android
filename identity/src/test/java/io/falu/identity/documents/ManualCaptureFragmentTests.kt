package io.falu.identity.documents

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
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.capture.CaptureDocumentViewModel
import io.falu.identity.capture.manual.ManualCaptureFragment
import io.falu.identity.databinding.FragmentManualCaptureBinding
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals
import com.google.android.material.R as MatR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ManualCaptureFragmentTests {
    private val uri = mock<Uri>()

    private val documentUploadDisposition = MutableLiveData(DocumentUploadDisposition())

    private val mockCaptureDocumentViewModel = mock<CaptureDocumentViewModel> {}
    private val modelFile = MutableLiveData<File>()

    private val mockIdentityVerificationViewModel = com.nhaarman.mockitokotlin2.mock<IdentityVerificationViewModel> {
        com.nhaarman.mockitokotlin2.whenever(it.documentUploadDisposition).thenReturn(documentUploadDisposition)
        on { it.documentDetectorModelFile } doReturn (modelFile)
    }

    @Test
    fun `test if result callbacks are initialized and UI correctness for ID cards and DLs`() {
        launchManualFragment { binding, _, fragment ->
            val callbackCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()

            verify(mockCaptureDocumentViewModel).captureDocumentImages(
                same(fragment),
                any(),
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
        launchManualFragment(IdentityDocumentType.PASSPORT) { binding, _, fragment ->
            val callbackCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()

            verify(mockCaptureDocumentViewModel).captureDocumentImages(
                same(fragment),
                any(),
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
    fun `test if image capture works for id and dl`() {
        captureImage()
    }

    @Test
    fun `test if image capture works for passport`() {
        captureImage(IdentityDocumentType.PASSPORT)
    }

    private fun captureImage(
        documentType: IdentityDocumentType = IdentityDocumentType.IDENTITY_CARD
    ) {
        launchManualFragment(documentType) { binding, _, fragment ->
            val frontImageCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()
            val backImageImageCaptor: KArgumentCaptor<(Uri) -> Unit> = argumentCaptor()

            verify(mockCaptureDocumentViewModel).captureDocumentImages(
                same(fragment),
                any(),
                frontImageCaptor.capture(),
                backImageImageCaptor.capture()
            )

            binding.buttonSelectFront.callOnClick()

            verify(mockCaptureDocumentViewModel).captureImageFront(fragment.requireContext())
            frontImageCaptor.lastValue(uri)

            binding.buttonSelectBack.callOnClick()

            if (documentType != IdentityDocumentType.PASSPORT) {
                verify(mockCaptureDocumentViewModel).captureImageBack(fragment.requireContext())
                backImageImageCaptor.lastValue(uri)
            }
        }
    }

    private fun launchManualFragment(
        documentType: IdentityDocumentType = IdentityDocumentType.IDENTITY_CARD,
        block: (
            binding: FragmentManualCaptureBinding,
            navController: TestNavHostController,
            fragment: AbstractCaptureFragment
        ) -> Unit
    ) {
        launchFragmentInContainer(
            bundleOf(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE to documentType),
            themeResId = MatR.style.Theme_MaterialComponents
        ) {
            ManualCaptureFragment(createFactoryFor(mockIdentityVerificationViewModel)).also {
                it.captureDocumentViewModelFactory = createFactoryFor(mockCaptureDocumentViewModel)
            }
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_manual_capture)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentManualCaptureBinding.bind(it.requireView()), navController, it)
        }
    }
}