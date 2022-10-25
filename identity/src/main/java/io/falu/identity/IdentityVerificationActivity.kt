package io.falu.identity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationStatus
import io.falu.identity.databinding.ActivityIdentityVerificationBinding
import io.falu.identity.utils.FileUtils
import software.tingle.api.HttpApiResponseProblem

internal class IdentityVerificationActivity : AppCompatActivity(),
    IdentityVerificationResultCallback {

    private val verificationViewModel: IdentityVerificationViewModel by viewModels {
        IdentityVerificationViewModel.factoryProvider(this, apiClient, fileUtils, contractArgs)
    }

    private val contractArgs by lazy {
        requireNotNull(ContractArgs.getFromIntent(intent)) {
            "Arguments are required."
        }
    }

    private val binding by lazy {
        ActivityIdentityVerificationBinding.inflate(layoutInflater)
    }

    private val fileUtils by lazy {
        FileUtils(this)
    }

    private lateinit var apiClient: IdentityVerificationApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        apiClient =
            IdentityVerificationApiClient(this, contractArgs.temporaryKey, BuildConfig.DEBUG)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setNavigationController()

        binding.ivIdentityVerification.setImageURI(contractArgs.workspaceLogo)

        verificationViewModel.fetchVerification(
            onFailure = {
                finishWithVerificationResult(IdentityVerificationResult.Failed(it))
            }
        )
        verificationViewModel.fetchSupportedCountries()
        verificationViewModel.observeForVerificationResults(
            this,
            onSuccess = { onVerificationSuccessful(it) },
            onError = { onVerificationFailure(it) })
    }

    private fun onVerificationSuccessful(verification: Verification) {
        binding.tvWorkspaceName.text = verification.workspace.name
        binding.viewLive.visibility = if (verification.live) View.VISIBLE else View.GONE
        binding.viewSandbox.viewSandbox.visibility =
            if (verification.live) View.GONE else View.VISIBLE

        when (verification.status) {
            VerificationStatus.INPUT_REQUIRED -> {
            }
            VerificationStatus.PROCESSING,
            VerificationStatus.COMPLETED -> onFinishWithResult(
                IdentityVerificationResult.Succeeded
            )
            VerificationStatus.CANCELLED -> onFinishWithResult(IdentityVerificationResult.Canceled)
        }
    }

    private fun onVerificationFailure(error: HttpApiResponseProblem?) {
        // TODO: Navigate to error page
    }

    private fun finishWithVerificationResult(result: IdentityVerificationResult) {
        val intent = Intent()
        result.addToIntent(intent)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onFinishWithResult(result: IdentityVerificationResult) {
        val intent = Intent()
        result.addToIntent(intent)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setNavigationController() {
        //
        supportActionBar?.hide()

        //
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        //
    }
}