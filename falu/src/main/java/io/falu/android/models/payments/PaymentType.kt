package io.falu.android.models.payments

import com.google.gson.annotations.SerializedName

enum class PaymentType {
    @SerializedName("mpesa")
    MPESA,

    @SerializedName("airtelmoney")
    AIRTEL_MONEY,

    @SerializedName("mtnmoney")
    MTN_MONEY,

    @SerializedName("pesalink")
    PESALINK
}