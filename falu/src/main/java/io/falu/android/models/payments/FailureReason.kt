package io.falu.android.models.payments

import com.google.gson.annotations.SerializedName

enum class FailureReason {
    @SerializedName("unknown")
    UNKNOWN,

    @SerializedName("insufficient_balance")
    INSUFFICIENT_BALANCE,

    @SerializedName("authentication_error")
    AUTHENTICATION_ERROR,

    @SerializedName("timeout")
    TIMEOUT,

    @SerializedName("other")
    OTHER
}