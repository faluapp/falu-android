package io.falu.identity.navigation

internal class InitialDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val INITIAL = "initial"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = INITIAL
        }
    }
}