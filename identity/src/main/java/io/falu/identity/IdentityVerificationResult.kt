package io.falu.identity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize

/**
 * A class  for Falu Identity SDK
 */
sealed class IdentityVerificationResult : Parcelable {
    /**
     * Called when the identity verification process completes without error.
     */
    @Parcelize
    object Succeeded : IdentityVerificationResult()

    /**
     * Called when the identity verification process is canceled.
     */
    @Parcelize
    object Canceled : IdentityVerificationResult()

    /**
     * Called when the was an error with identity verification process
     *
     * @param throwable The error that occurred
     */
    @Parcelize
    class Failed(val throwable: Throwable) : IdentityVerificationResult()

    internal fun addToIntent(intent: Intent?): Intent? {
        return intent?.putExtra(INTENT_EXTRA_KEY, this)
    }

    @JvmSynthetic
    fun addToBundle() = bundleOf(INTENT_EXTRA_KEY to this)

    internal companion object {
        private const val INTENT_EXTRA_KEY = "extras:verification-results"

        fun getFromBundle(bundle: Bundle?): IdentityVerificationResult {
            return bundle?.getParcelable(INTENT_EXTRA_KEY)
                ?: Failed(IllegalStateException("Could not get verification result from intent"))
        }

        fun getFromIntent(intent: Intent?): IdentityVerificationResult {
            return intent?.getParcelableExtra(INTENT_EXTRA_KEY)
                ?: Failed(IllegalStateException("Could not get verification result from intent"))
        }
    }
}