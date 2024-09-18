package io.falu.identity.welcome

import android.content.Context
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
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.requirements.Requirement
import io.falu.identity.api.models.requirements.RequirementType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.databinding.FragmentWelcomeBinding
import io.falu.identity.utils.createFactoryFor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import com.google.android.material.R as MatR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class WelcomeFragmentTest {
    private val mockVerificationResultCallback = mock<IdentityVerificationResultCallback>()

    private val verification = mock<Verification>().also {
        whenever(it.workspace).thenReturn(
            WorkspaceInfo("Test", "ken")
        )
        whenever(it.requirements).thenReturn(
            Requirement(pending = mutableListOf(RequirementType.CONSENT), errors = mutableListOf())
        )
        whenever(it.remainingAttempts).thenReturn(null)
    }

    private val mockIdentityVerificationViewModel = mock<IdentityVerificationViewModel> {
        on { contractArgs }.thenReturn(contractArgs)

        on { apiClient }.thenReturn(
            IdentityVerificationApiClient(
                context = ApplicationProvider.getApplicationContext(),
                apiKey = temporaryKey,
                maxNetworkRetries = 0,
                enableLogging = true
            )
        )

        on { analyticsRequestBuilder }.thenReturn(
            IdentityAnalyticsRequestBuilder(
                context = ApplicationProvider.getApplicationContext(),
                args = contractArgs
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
    fun `test if verification data is loading`() {
        launchWelcomeFragment { binding, _ ->
            assertEquals(binding.progressView.visibility, View.VISIBLE)
            assertEquals(binding.scrollView.visibility, View.GONE)
            assertEquals(binding.viewButtons.visibility, View.GONE)
        }
    }

    @Test
    fun `test if verification data is displayed correctly`() {
        launchWelcomeFragment { binding, _ ->
            successfulVerification()

            assertEquals(binding.progressView.visibility, View.GONE)
            assertEquals(binding.scrollView.visibility, View.VISIBLE)
            assertEquals(binding.viewButtons.visibility, View.VISIBLE)

            assertEquals(
                binding.tvWelcomeSubtitle.text,
                (ApplicationProvider.getApplicationContext() as Context).getString(
                    R.string.welcome_subtitle,
                    verification.workspace.name
                )
            )
        }
    }

    @Test
    fun `test if consent agreed to and update verification data`() {
        launchWelcomeFragment { binding, _ ->
            binding.buttonAccept.findViewById<MaterialButton>(R.id.button_loading).callOnClick()

            verify(mockIdentityVerificationViewModel).updateVerification(any(), any(), any(), any())

            assertEquals(
                binding.buttonAccept.findViewById<MaterialButton>(R.id.button_loading).isEnabled,
                false
            )
            assertEquals(
                binding.buttonAccept.findViewById<ProgressBar>(R.id.progress_view).visibility,
                View.VISIBLE
            )
        }
    }

    private fun launchWelcomeFragment(
        block: (binding: FragmentWelcomeBinding, navController: TestNavHostController) -> Unit
    ) {
        launchFragmentInContainer(themeResId = MatR.style.Theme_MaterialComponents) {
            WelcomeFragment(
                createFactoryFor(mockIdentityVerificationViewModel),
                mockVerificationResultCallback
            )
        }.onFragment {
            val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

            navController.setGraph(R.navigation.identity_verification_nav_graph)

            navController.setCurrentDestination(R.id.fragment_welcome)

            Navigation.setViewNavController(it.requireView(), navController)

            block(FragmentWelcomeBinding.bind(it.requireView()), navController)
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