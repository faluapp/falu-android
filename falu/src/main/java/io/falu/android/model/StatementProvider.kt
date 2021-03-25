package io.falu.android.model

enum class StatementProvider {
    MPESA;

    val desc: String
        get() {
            return when (this) {
                MPESA -> "mpesa"
            }
        }
}