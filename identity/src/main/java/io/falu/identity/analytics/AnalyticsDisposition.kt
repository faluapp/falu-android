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
        return when {
            modification.backModelScore != null -> this.copy(backModelScore = modification.backModelScore)
            modification.uploadMethod != null -> this.copy(uploadMethod = modification.uploadMethod)
            modification.frontModelScore != null -> this.copy(frontModelScore = modification.frontModelScore)
            modification.scanType != null -> this.copy(scanType = modification.scanType)
            modification.selfieModelScore != null -> this.copy(selfieModelScore = modification.selfieModelScore)
            modification.selfie != null -> this.copy(selfie = selfie)
            else -> this
        }
    }
}