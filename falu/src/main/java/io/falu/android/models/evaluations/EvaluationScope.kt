package io.falu.android.models.evaluations

import com.google.gson.annotations.SerializedName

enum class EvaluationScope {
    @SerializedName("personal")
    PERSONAL,

    @SerializedName("business")
    BUSINESS;

    val description: String
        get() {
            return when (this) {
                PERSONAL -> "personal"
                BUSINESS -> "business"
            }
        }
}