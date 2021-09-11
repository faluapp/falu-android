package io.falu.android.models.files

import okhttp3.MediaType
import java.io.File
import java.util.*


data class UploadRequest(
    /**
     * Contents of the file. It should follow the specifications of RFC 2388.
     */
    var file: File,

    /**
     * Purpose for a file
     */
    var purpose: FilePurpose,
    /**
     * Time at which the file expires.
     */
    var date: Date? = null,
    var description: String? = null
) {
    internal lateinit var mediaType: MediaType
}
