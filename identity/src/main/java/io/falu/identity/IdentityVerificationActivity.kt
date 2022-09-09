package io.falu.identity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.falu.identity.databinding.ActivityIdentityVerificationBinding

internal class IdentityVerificationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityIdentityVerificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}