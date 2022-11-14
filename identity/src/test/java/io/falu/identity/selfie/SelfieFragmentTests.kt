package io.falu.identity.selfie

import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.button.MaterialButton
import io.falu.identity.R
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.VerificationDocumentSide
import io.falu.identity.api.models.verification.VerificationDocumentUpload
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraView
import io.falu.identity.capture.CaptureDocumentViewModel
import io.falu.identity.databinding.FragmentSelfieBinding
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SelfieFragmentTests {

    private val verificationUploadRequest =
        VerificationUploadRequest(
            consent = true,
            country = "ken",
            document = VerificationDocumentUpload(
                type = IdentityDocumentType.IDENTITY_CARD,
                front = VerificationDocumentSide(UploadMethod.MANUAL, file = "file_1234")
            )
        )

    private val mockCaptureDocumentViewModel = mock<CaptureDocumentViewModel> {}

    @Test
    fun `test UI correctness on first launch`() {
        launchSelfieFragment { binding, _ ->
            assertEquals(binding.viewSelfieCamera.visibility, View.VISIBLE)
            assertEquals(binding.viewSelfieResult.visibility, View.GONE)
        }
    }

    @Test
    fun `test taking selfie and results UI`() {
        launchSelfieFragment { binding, _ ->
            val imageCaptor: KArgumentCaptor<(Uri?) -> Unit> = argumentCaptor()

            binding.buttonTakeSelfie.callOnClick()

            val mockCameraView = mock<CameraView>()

            verify(mockCameraView).takePhoto(
                imageCaptor.capture(),
                any()
            )

            assertEquals(binding.viewSelfieCamera.visibility, View.GONE)
            assertEquals(binding.viewSelfieResult.visibility, View.VISIBLE)
            assertEquals(
                binding.buttonContinue.findViewById<MaterialButton>(R.id.button_loading).isEnabled,
                true
            )
            assertEquals(
                binding.buttonContinue.findViewById<ProgressBar>(R.id.progress_view).visibility,
                View.GONE
            )
        }
    }

    private fun launchSelfieFragment(block: (binding: FragmentSelfieBinding, navController: TestNavHostController) -> Unit) {
        launchFragmentInContainer(
            verificationUploadRequest.addToBundle(),
            themeResId = R.style.Theme_MaterialComponents
        ) {
            SelfieFragment(createFactoryFor(mockCaptureDocumentViewModel))
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_selfie)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentSelfieBinding.bind(it.requireView()), navController)
        }
    }
}