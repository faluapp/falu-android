package tingle.software.falu.model

enum class StatementProvider {
    MPESA;

    val desc: String
        get() {
            return when (this) {
                MPESA -> "mpesa"
            }
        }
}