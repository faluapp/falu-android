package io.falu.identity.api.models.verification

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import io.falu.identity.R

internal enum class Gender {
    @SerializedName("male")
    MALE,

    @SerializedName("female")
    FEMALE;

    internal val desc: Int
        @StringRes
        get() {
            return when (this) {
                MALE -> R.string.document_verification_gender_male
                FEMALE -> R.string.document_verification_gender_female
            }
        }
}