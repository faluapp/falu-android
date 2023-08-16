package io.falu.core.models

import java.io.File
import java.util.Date

/**
 * Falu requires request of type `multipart/form-data` when uploading files.
 * The request contains the file being uploaded, and the other arguments for file creation.
 */
data class FaluFileUploadArgs(
    /**
     * Contents of the file. It should follow the specifications of RFC 2388.
     */
    val file: File,

    /**
     * Purpose for a file, possible values include:- `business.icon`, `business.logo`,`customer.signature`,
     * `customer.selfie` `customer.tax.document`,`message.media`,`identity.document`,`identity.video`
     */
    val purpose: String,

    /**
     * Time at which the file expires.
     */
    val date: Date? = null,

    /***/
    val description: String? = null
)