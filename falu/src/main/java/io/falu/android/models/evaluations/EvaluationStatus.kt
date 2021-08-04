package io.falu.android.models.evaluations

import com.google.gson.annotations.SerializedName

enum class EvaluationStatus {
    @SerializedName("created")
    CREATED,
    @SerializedName("scoring")
    SCORING,
    @SerializedName("completed")
    COMPLETED,
    @SerializedName("failed")
    FAILED
}