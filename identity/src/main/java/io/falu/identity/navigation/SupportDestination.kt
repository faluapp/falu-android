package io.falu.identity.navigation

internal class SupportDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val SUPPORT = "support"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = SUPPORT
        }
    }
}