package io.falu.core.models

import androidx.annotation.RestrictTo
import okhttp3.MediaType
import java.io.File
import java.util.*

/**
 * Falu requires request of type `multipart/form-data` when uploading files.
 * The request contains the file being uploaded, and the other arguments for file creation.
 */
data class FaluFileUploadArgs(
    /**
     * Contents of the file. It should follow the specifications of RFC 2388.
     */
    internal var file: File,

    /**
     * Purpose for a file, possible values include:- `business.icon`, `business.logo`,`customer.signature`,
     * `customer.selfie` `customer.tax.document`,`message.media`,`identity.document`,`identity.video`
     */
    internal var purpose: String,

    /**
     * Time at which the file expires.
     */
    internal var date: Date? = null,
) {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal lateinit var mediaType: MediaType
}
