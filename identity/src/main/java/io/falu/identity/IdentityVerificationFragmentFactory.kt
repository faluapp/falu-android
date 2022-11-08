package io.falu.identity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.falu.identity.confirmation.ConfirmationFragment

internal class IdentityVerificationFragmentFactory(private val callback: IdentityVerificationResultCallback) :
    FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ConfirmationFragment::class.java.name -> ConfirmationFragment(callback)
            else -> super.instantiate(classLoader, className)
        }
    }
}