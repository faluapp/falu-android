package io.falu.identity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import io.falu.identity.databinding.ActivityIdentityVerificationBinding

internal class IdentityVerificationActivity : AppCompatActivity() {
    private val verificationViewModel: IdentityVerificationViewModel by viewModels()

    private val binding by lazy {
        ActivityIdentityVerificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val contractArgs = ContractArgs.getFromIntent(intent)!!
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