package io.falu.identity.support

import android.net.Uri
import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import io.falu.identity.ContractArgs
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.models.Support
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.databinding.FragmentSupportBinding
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
class SupportFragmentTest {

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
            )
        )
    }

    private val verification = mock<Verification>().also {
        whenever(it.support).thenReturn(
            Support(
                email = "support@example.com",
                phone = "+2547123456789",
                url = "https://support.example.com"
            )
        )
    }

    private fun successfulVerification(data: Verification = verification) {
        val successCaptor: KArgumentCaptor<(Verification) -> Unit> = argumentCaptor()
        verify(mockIdentityVerificationViewModel, times(1)).observeForVerificationResults(
            any(),
            successCaptor.capture(),
            any()
        )
        successCaptor.lastValue(data)
    }

    @Test
    fun `test loading of support data and initiate calls and emails`() {
        launchSupportFragment { binding, _ ->
            successfulVerification()

            val support = verification.support!!

            assertEquals(binding.tvSupportUrl.text, support.url)

            binding.viewSupportCall.callOnClick()

            binding.viewSupportEmail.callOnClick()
        }
    }

    private fun launchSupportFragment(
        block: (binding: FragmentSupportBinding, navController: TestNavHostController) -> Unit
    ) {
        launchFragmentInContainer(themeResId = MatR.style.Theme_MaterialComponents) {
            SupportFragment(
                createFactoryFor(mockIdentityVerificationViewModel)
            )
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_support)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentSupportBinding.bind(it.requireView()), navController)
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