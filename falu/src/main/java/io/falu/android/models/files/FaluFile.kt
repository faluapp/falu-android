package io.falu.android.models.files

import com.google.gson.annotations.JsonAdapter
import io.falu.android.models.FaluModel
import kotlinx.parcelize.Parcelize
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.*

@Parcelize
data class FaluFile(
    /**
     * Unique identifier for the object.
     */
    var id: String,
    /**
     * Time at which the object was created.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var created: Date,
    /**
     * Time at which the object was last updated.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var updated: Date?,
    /**
     * Purpose for a file
     */
    var purpose: String?,
    /**
     * Purpose for a file
     */
    var type: String?,
    /**
     * A name of the file suitable for saving to a filesystem.
     */
    var fileName: String?,
    /**
     * Size in bytes of the file
     */
    var size: Int?,
    /**
     * Time at which the file expires
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var expires: Date?
) : FaluModel()
