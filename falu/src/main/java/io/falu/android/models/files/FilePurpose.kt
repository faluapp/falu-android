package io.falu.android.models.files

import com.google.gson.annotations.SerializedName

enum class FilePurpose {
    @SerializedName("business.icon")
    BUSINESS_ICON,

    @SerializedName("business.logo")
    BUSINESS_LOGO,

    @SerializedName("customer.signature")
    CUSTOMER_SIGNATURE,

    @SerializedName("customer.selfie")
    CUSTOMER_SELFIE,

    @SerializedName("customer.tax.document")
    CUSTOMER_TAX_DOCUMENT,

    @SerializedName("customer.evaluation")
    CUSTOMER_EVALUATION,

    @SerializedName("identity.document")
    IDENTITY_DOCUMENT;

    internal val purpose: String
        get() {
            return when (this) {
                BUSINESS_ICON -> "business.icon"
                BUSINESS_LOGO -> "business.logo"
                CUSTOMER_SIGNATURE -> "customer.signature"
                CUSTOMER_SELFIE -> "customer.selfie"
                CUSTOMER_TAX_DOCUMENT -> "customer.tax.document"
                CUSTOMER_EVALUATION -> "customer.evaluation"
                IDENTITY_DOCUMENT -> "identity.document"
            }
        }
}