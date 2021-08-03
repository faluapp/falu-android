package io.falu.android.model.evaluations

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import kotlinx.parcelize.Parcelize
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.*

/**
 * Represents the scoring result for an evaluation.
 */
@Parcelize
data class Scoring(
    /**
     * Risk probability. The higher the value, the higher the risk
     */
    var risk: Float?,

    /**
     * Limit advised for lending in the smallest currency unit.
     */
    var limit: Float?,

    /**
     * Risk probability. The higher the value, the higher the risk
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var expires: Date?
) : Parcelable
