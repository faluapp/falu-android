package io.falu.identity.api

import android.os.Parcelable
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.VerificationDocumentSide
import io.falu.identity.api.models.verification.VerificationDocumentUpload
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.api.models.verification.VerificationUploadResult
import kotlinx.parcelize.Parcelize

/**
 * Represents the upload state of the back and front of documents
 */
@Parcelize
internal data class DocumentUploadDisposition(
    var front: VerificationUploadResult? = null,
    var back: VerificationUploadResult? = null
) : Parcelable {

    fun modify(
        documentSide: DocumentSide,
        result: VerificationUploadResult
    ) =
        if (documentSide == DocumentSide.FRONT) {
            this.copy(front = result)
        } else {
            this.copy(back = result)
        }

    val isFrontUpload: Boolean
        get() = front != null

    val isBackUploaded: Boolean
        get() = back != null

    val isBothUploadLoad: Boolean
        get() = front != null && back != null

    fun generateVerificationUploadRequest(identityDocumentType: IdentityDocumentType):
            VerificationUploadRequest {
        val front = VerificationDocumentSide(
            method = front!!.method!!,
            file = front!!.file.id,
            score = front!!.score
        )
        val back = if (identityDocumentType == IdentityDocumentType.PASSPORT) {
            null
        } else {
            VerificationDocumentSide(
                method = back!!.method!!,
                file = back!!.file.id,
                score = back!!.score
            )
        }

        val document = VerificationDocumentUpload(
            type = identityDocumentType,
            front = front,
            back = back
        )

        return VerificationUploadRequest(document = document)
    }
}