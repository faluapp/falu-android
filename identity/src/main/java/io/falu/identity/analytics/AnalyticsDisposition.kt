package io.falu.identity.analytics

import android.os.Parcelable
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.scan.ScanDisposition
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AnalyticsDisposition(
    val backModelScore: Float? = null,
    val uploadMethod: UploadMethod? = null,
    val frontModelScore: Float? = null,
    val scanType: ScanDisposition.DocumentScanType? = null,
    val selfieModelScore: Float? = null,
    val selfie: Boolean? = null,
) : Parcelable {

    fun modify(modification: AnalyticsDisposition): AnalyticsDisposition {
        return this.copy(
            backModelScore = modification.backModelScore,
            uploadMethod = modification.uploadMethod,
            frontModelScore = modification.frontModelScore,
            scanType = modification.scanType,
            selfieModelScore = modification.selfieModelScore,
            selfie = modification.selfie
        )
    }
}