package io.falu.identity.capture

import android.os.Build
import androidx.navigation.testing.TestNavHostController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.databinding.FragmentUploadCaptureBinding
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class UploadCaptureFragmentTest {

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {}

    private fun launchUploadFragment(block: (binding: FragmentUploadCaptureBinding, navController: TestNavHostController) -> Unit) {

    }

}