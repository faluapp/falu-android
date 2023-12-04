package io.falu.identity.confirmation

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.capture.scan.DocumentScanViewModel
import io.falu.identity.databinding.FragmentConfirmationBinding
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.android.material.R as MatR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ConfirmationFragmentTest {
    private val mockVerificationResultCallback = mock<IdentityVerificationResultCallback>()
    private val identityVerificationViewModel = mock<IdentityVerificationViewModel>()

    @Test
    fun testButtonClickFinishesWithComplete() {
        launchConfirmationFragment { binding, _ ->
            binding.buttonFinish.callOnClick()

            verify(mockVerificationResultCallback).onFinishWithResult(
                eq(IdentityVerificationResult.Succeeded)
            )
        }
    }

    private fun launchConfirmationFragment(
        block: (binding: FragmentConfirmationBinding, navController: TestNavHostController) -> Unit
    ) =
        launchFragmentInContainer(themeResId = MatR.style.Theme_MaterialComponents) {
            ConfirmationFragment(
                createFactoryFor(identityVerificationViewModel),
                mockVerificationResultCallback
            )
        }.onFragment {

            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_welcome)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentConfirmationBinding.bind(it.requireView()), navController)
        }
}