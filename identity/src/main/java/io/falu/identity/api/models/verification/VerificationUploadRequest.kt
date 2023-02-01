package io.falu.identity.api.models.verification

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import io.falu.identity.IdentityVerificationResult
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationUploadRequest(
    var consent: Boolean = true,
    var country: String? = null,
    var document: VerificationDocumentUpload,
    var selfie: VerificationSelfieUpload? = null
) : Parcelable {

    @JvmSynthetic
    fun addToBundle() = bundleOf(KEY_VERIFICATION_UPLOAD_REQUEST to this)

    internal companion object {
        private const val KEY_VERIFICATION_UPLOAD_REQUEST = ":verification"

        fun getFromBundle(bundle: Bundle?): VerificationUploadRequest? {
            return bundle?.getParcelable(KEY_VERIFICATION_UPLOAD_REQUEST)
        }

        fun getFromIntent(intent: Intent?): IdentityVerificationResult? {
            return intent?.getParcelableExtra(KEY_VERIFICATION_UPLOAD_REQUEST)
        }
    }
}