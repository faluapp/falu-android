package io.falu.android.models

enum class StatementProvider {
    MPESA;

    val desc: String
        get() {
            return when (this) {
                MPESA -> "mpesa"
            }
        }
}