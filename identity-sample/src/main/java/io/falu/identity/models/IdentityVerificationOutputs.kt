package io.falu.identity.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class IdentityVerificationOutputs(
    /**
     * The user’s verified id number type.
     */
    @SerializedName("id_number_type")
    var idNumberType: String?,

    /**
     * The user’s verified id number.
     */
    @SerializedName("id_number")
    var idNumber: String?,

    /**
     * The user’s verified first name.
     */
    @SerializedName("first_name")
    var firstName: String?,

    /**
     * The user’s verified last name.
     */
    @SerializedName("last_name")
    var lastName: String?,

    /**
     * The user’s verified date of birth.
     */
    var birthday: Date?,

    /**
     * The user’s other verified names.
     */
    @SerializedName("other_names")
    var otherNames: MutableList<String>?
)