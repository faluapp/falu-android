package io.falu.identity.api.models

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import io.falu.identity.R

internal enum class IdentityDocumentType {
    @SerializedName("driving_license")
    IDENTITY_CARD,

    @SerializedName("driving_license")
    PASSPORT,

    @SerializedName("driving_license")
    DRIVING_LICENSE;

    val titleRes: Int
        @StringRes
        get() {
            return when (this) {
                IDENTITY_CARD -> R.string.document_selection_document_identity_card
                PASSPORT -> R.string.document_selection_document_passport
                DRIVING_LICENSE -> R.string.document_selection_document_driver_license
            }
        }
}