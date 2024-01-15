package io.falu.identity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.fragment.NavHostFragment
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationStatus
import io.falu.identity.databinding.ActivityIdentityVerificationBinding
import io.falu.identity.utils.FileUtils

internal class IdentityVerificationActivity : AppCompatActivity(),
    IdentityVerificationResultCallback {

    @VisibleForTesting
    internal val factory =
        IdentityVerificationViewModel.factoryProvider(
            this,
            { apiClient },
            { analyticsRequestBuilder },
            { fileUtils },
            { contractArgs }
        )

    private val verificationViewModel: IdentityVerificationViewModel by viewModels {
        factory
    }

    private val binding by lazy {
        ActivityIdentityVerificationBinding.inflate(layoutInflater)
    }

    private val contractArgs: ContractArgs by lazy {
        requireNotNull(ContractArgs.getFromIntent(intent)) {
            "Arguments are required."
        }
    }

    private val fileUtils: FileUtils by lazy {
        FileUtils(this)
    }

    private val apiClient: IdentityVerificationApiClient by lazy {
        IdentityVerificationApiClient(this, contractArgs.temporaryKey, BuildConfig.DEBUG)
    }

    private val analyticsRequestBuilder: IdentityAnalyticsRequestBuilder by lazy {
        IdentityAnalyticsRequestBuilder(this, contractArgs)
    }

    private lateinit var launchFallbackUrl: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        supportFragmentManager.fragmentFactory =
            IdentityVerificationFragmentFactory(this, factory)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setNavigationController()
        setupFallbackLauncher()

        verificationViewModel.loadUriToImageView(
            contractArgs.workspaceLogo,
            binding.ivIdentityVerification
        )

        verificationViewModel.fetchVerification(onFailure = {
            finishWithVerificationResult(IdentityVerificationResult.Failed(it))
        })

        verificationViewModel.fetchSupportedCountries()
        verificationViewModel.observeForVerificationResults(
            this,
            onSuccess = {

                if (savedInstanceState?.getBoolean(KEY_OPENED, false) != true) {
                    verificationViewModel.reportTelemetry(verificationViewModel.analyticsRequestBuilder.viewOpened())
                }

                if (!it.supported) {
                    launchFallbackUrl(it.url.orEmpty())
                } else {
                    onVerificationSuccessful(it)
                }
            },
            onError = { onVerificationFailure(false, it) })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_OPENED, true)
    }

    private fun onVerificationSuccessful(verification: Verification) {
        binding.tvWorkspaceName.text = verification.workspace.name
        binding.viewLive.visibility = if (verification.live) View.VISIBLE else View.GONE
        binding.tvSupport.visibility = if (verification.support != null) View.VISIBLE else View.GONE
        binding.viewSandbox.viewSandbox.visibility =
            if (verification.live) View.GONE else View.VISIBLE

        when (verification.status) {
            VerificationStatus.INPUT_REQUIRED -> {
            }

            VerificationStatus.PROCESSING,
            VerificationStatus.VERIFIED -> onFinishWithResult(
                IdentityVerificationResult.Succeeded
            )

            VerificationStatus.CANCELLED -> onFinishWithResult(IdentityVerificationResult.Canceled)
        }
    }

    private fun onVerificationFailure(fromFallbackUrl: Boolean, throwable: Throwable?) {
        verificationViewModel.reportTelemetry(
            verificationViewModel.analyticsRequestBuilder.verificationFailed(
                fromFallbackUrl,
                throwable = throwable
            )
        )
    }

    private fun finishWithVerificationResult(result: IdentityVerificationResult) {

        verificationViewModel.reportTelemetry(
            verificationViewModel.analyticsRequestBuilder.viewClosed(result.javaClass.name)
        )

        val intent = Intent()
        result.addToIntent(intent)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setupFallbackLauncher() {
        launchFallbackUrl =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                verificationViewModel.fetchVerification {
                    finishWithVerificationResult(IdentityVerificationResult.Failed(it))
                }
                verificationViewModel.observeForVerificationResults(this,
                    onSuccess = {
                        if (it.submitted) {
                            finishWithVerificationResult(IdentityVerificationResult.Succeeded)
                        } else {
                            verificationViewModel.reportTelemetry(
                                verificationViewModel.analyticsRequestBuilder.verificationCanceled(true)
                            )
                            finishWithVerificationResult(IdentityVerificationResult.Canceled)
                        }
                    },
                    onError = {
                        onVerificationFailure(true, it)
                    }
                )
            }
    }

    private fun launchFallbackUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .build()
        customTabsIntent.intent.data = Uri.parse(url)
        launchFallbackUrl.launch(customTabsIntent.intent)
    }

    override fun onFinishWithResult(result: IdentityVerificationResult) {
        finishWithVerificationResult(result)
    }

    private fun setNavigationController() {
        //
        supportActionBar?.hide()

        //
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        //
        binding.tvSupport.setOnClickListener {
            navController.navigate(R.id.action_global_fragment_support)
        }
    }

    companion object {
        private const val KEY_OPENED = ":opened"
    }
}