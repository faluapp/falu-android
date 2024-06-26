package io.falu.identity.api.models.country

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.falu.identity.api.models.IdentityDocumentType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Country(
    var code: String,
    var name: String,
    @SerializedName("flag_url")
    var flag: String
) : Parcelable

@Parcelize
internal data class SupportedCountry(
    var country: Country,
    var documents: MutableList<IdentityDocumentType>
) : Parcelable