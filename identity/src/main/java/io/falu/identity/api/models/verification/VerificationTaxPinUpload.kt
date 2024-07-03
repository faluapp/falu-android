package io.falu.identity.api.models.verification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationTaxPinUpload(
    val type: String = "ken_pin",
    val value: String
) : Parcelable
