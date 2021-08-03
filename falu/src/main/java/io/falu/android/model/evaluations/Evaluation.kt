package io.falu.android.model.evaluations

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import kotlinx.parcelize.Parcelize
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.*

@Parcelize
data class Evaluation constructor(
    var id: String,
    var currency: String?,
    var scope: EvaluationScope?,
    @JsonAdapter(ISO8601DateAdapter::class)
    var created: Date?,
    @JsonAdapter(ISO8601DateAdapter::class)
    var updated: Date?
) : Parcelable

