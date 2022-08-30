package io.falu.android.models

import com.google.gson.annotations.SerializedName

data class PhysicalAddress(
    /**
     * The first line.
     * Also referred to as the <c>street-address</c>.
     */
    var line1: String,

    /**
     * The second line.
     * Also referred to as the <c>apt, building, suite no. etc.</c>
     */
    var line2: String?,

    /**
     * The city.
     */
    var city: String,

    /**
     * The postal code or zip code.
     * Each country has its way of denoting postal codes.
     */
    @SerializedName("postal_code")
    var postalCode: String?,

    /**
     * The state or province.
     * Also referred to as the <c>province</c>
     */
    var state: String?,

    /**
     * The country.
     */
    var country: String
)
