package io.falu.core.models

import android.os.Parcelable
import androidx.annotation.RestrictTo
import kotlinx.parcelize.Parcelize

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
open class FaluModel : Parcelable {
    /**
     * Unique identifier for the workspace that the object belongs to.
     */
    var workspace: String? = null

    /**
     * An optional arbitrary string attached to the object.
     * Mainly used to describe the object and often useful for displaying to users.
     */
    var description: String? = null

    /**
     * Set of key-value pairs that you can attach to an object.
     * This can be useful for storing additional information about the object in a structured format.
     * The key can only contain alphanumeric, and ‘-’, ‘_’ characters, and the string has to start with a letter.
     */
    var metadata: Map<String, String>? = null
}