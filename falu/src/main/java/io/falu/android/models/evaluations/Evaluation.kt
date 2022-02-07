package io.falu.android.models.evaluations

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import kotlinx.parcelize.Parcelize
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.*

/**
 *  [The evaluation object](https://falu.io
 *  Represents a financial evaluation.
 */
@Parcelize
data class Evaluation constructor(
    /**
     * Unique identifier of the evaluation.
     */
    var id: String,

    /**
     * Three-letter ISO currency code, in lowercase.
     */
    var currency: String?,

    /**
     * Represents the scope within which an evaluation is generated.
     * This can also be considered the purpose of the evaluation.
     */
    var scope: String?,

    /**
     * Time at which the object was created.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var created: Date?,

    /**
     * Time at which the evaluation was last updated.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var updated: Date?,

    /**
     * Represents the status of an evaluation
     */
    var status: String?,

    /**
     * Indicates if this object belongs in the live environment
     */
    var live: Boolean?,

    /**
     * Unique identifier for the workspace that the evaluation belongs to.
     */
    var workspace: String,

    /**
     * Represents the scoring result for an evaluation.
     */
    var scoring: Scoring?,

    /**
     * Represents details about the statement used for an evaluation.
     */
    var statement: EvaluationStatement?
) : Parcelable

