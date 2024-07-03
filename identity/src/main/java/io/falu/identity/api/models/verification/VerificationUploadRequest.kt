package io.falu.identity.api.models.verification

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.google.gson.annotations.SerializedName
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.utils.parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationUploadRequest(
    var consent: Boolean = true,
    var country: String? = null,
    var document: VerificationDocumentUpload? = null,
    var selfie: VerificationSelfieUpload? = null,
    @SerializedName("id_number")
    var idNumber: VerificationIdNumberUpload? = null,
    @SerializedName("tax_id")
    var taxPin: VerificationTaxPinUpload? = null
) : Parcelable {

    @JvmSynthetic
    fun addToBundle() = bundleOf(KEY_VERIFICATION_UPLOAD_REQUEST to this)

    internal companion object {
        private const val KEY_VERIFICATION_UPLOAD_REQUEST = ":verification"

        fun getFromBundle(bundle: Bundle?): VerificationUploadRequest? {
            return bundle?.parcelable(KEY_VERIFICATION_UPLOAD_REQUEST)
        }

        fun getFromIntent(intent: Intent?): IdentityVerificationResult? {
            return intent?.parcelable(KEY_VERIFICATION_UPLOAD_REQUEST)
        }
    }
}