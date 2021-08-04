package io.falu.android.models.payments

import com.google.gson.annotations.SerializedName

enum class PaymentStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("succeeded")
    SUCCEEDED,

    @SerializedName("failed")
    FAILED
}
