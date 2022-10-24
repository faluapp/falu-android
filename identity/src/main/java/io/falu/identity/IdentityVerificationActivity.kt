package io.falu.identity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationStatus
import io.falu.identity.databinding.ActivityIdentityVerificationBinding
import io.falu.identity.utils.FileUtils
import software.tingle.api.HttpApiResponseProblem

internal class IdentityVerificationActivity : AppCompatActivity() {

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

        supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY_IDENTITY_VERIFICATION_RESULT,
            this
        ) { _, bundle ->
            val result = IdentityVerificationResult.getFromBundle(bundle)
            finishWithVerificationResult(result)
        }
    }

    private fun onVerificationSuccessful(verification: Verification) {
        binding.tvWorkspaceName.text = verification.workspace.name

        when (verification.status) {
            VerificationStatus.INPUT_REQUIRED -> {
            }
            VerificationStatus.PROCESSING,
            VerificationStatus.COMPLETED -> finishWithVerificationResult(
                IdentityVerificationResult.Succeeded
            )
            VerificationStatus.CANCELLED -> finishWithVerificationResult(IdentityVerificationResult.Canceled)
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

    private fun setNavigationController() {
        //
        supportActionBar?.hide()

        //
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        //
    }

    internal companion object {
        const val REQUEST_KEY_IDENTITY_VERIFICATION_RESULT = "key:identity-verification-result"
    }
}