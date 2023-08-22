package io.falu.identity.api.models.verification

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import io.falu.identity.api.models.IdentityDocumentType
import kotlinx.parcelize.Parcelize
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.Date

@Parcelize
internal data class VerificationIdNumberUpload(
    val type: IdentityDocumentType,
    val number: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @JsonAdapter(ISO8601DateAdapter::class)
    val birthday: Date,
    val sex: String
) : Parcelable