package io.falu.identity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.databinding.ActivityIdentityVerificationBinding

internal class IdentityVerificationActivity : AppCompatActivity() {

    private val verificationViewModel: IdentityVerificationViewModel by viewModels {
        IdentityVerificationViewModel.factoryProvider(this, apiClient)
    }

    private val contractArgs by lazy {
        requireNotNull(ContractArgs.getFromIntent(intent)) {
            "Arguments are required."
        }
    }

    private val binding by lazy {
        ActivityIdentityVerificationBinding.inflate(layoutInflater)
    }

    private lateinit var apiClient: IdentityVerificationApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        apiClient =
            IdentityVerificationApiClient(this, contractArgs.temporaryKey, BuildConfig.DEBUG)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setNavigationController()

        binding.ivIdentityVerification.setImageURI(contractArgs.workspaceLogo)
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