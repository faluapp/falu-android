package io.falu.identity.navigation

internal class TaxPinDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val TAX_PIN = "tax_pin"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = TAX_PIN
        }
    }
}