package io.falu.identity.navigation

internal class ConfirmationDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val CONFIRMATION = "confirmation"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = CONFIRMATION
        }
    }
}