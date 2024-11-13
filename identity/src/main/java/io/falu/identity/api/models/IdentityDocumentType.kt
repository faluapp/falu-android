package io.falu.identity.api.models

import android.content.Context
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import io.falu.identity.R
import io.falu.identity.scan.ScanDisposition

internal enum class IdentityDocumentType {
    @SerializedName("id_card")
    IDENTITY_CARD,

    @SerializedName("passport")
    PASSPORT,

    @SerializedName("driving_license")
    DRIVING_LICENSE;

    val titleRes: Int
        @StringRes
        get() {
            return when (this) {
                IDENTITY_CARD -> R.string.document_selection_document_identity_card
                PASSPORT -> R.string.document_selection_document_passport
                DRIVING_LICENSE -> R.string.document_selection_document_driver_license
            }
        }
}

internal fun IdentityDocumentType.getIdentityDocumentName(context: Context) =
    context.getString(this.titleRes)

internal fun IdentityDocumentType.getScanType():
    Pair<ScanDisposition.DocumentScanType, ScanDisposition.DocumentScanType?> {
    return when (this) {
        IdentityDocumentType.IDENTITY_CARD -> {
            Pair(
                ScanDisposition.DocumentScanType.ID_FRONT,
                ScanDisposition.DocumentScanType.ID_BACK
            )
        }

        IdentityDocumentType.PASSPORT -> {
            Pair(
                ScanDisposition.DocumentScanType.PASSPORT,
                null
            )
        }

        IdentityDocumentType.DRIVING_LICENSE -> {
            Pair(
                ScanDisposition.DocumentScanType.DL_FRONT,
                ScanDisposition.DocumentScanType.DL_BACK
            )
        }
    }
}