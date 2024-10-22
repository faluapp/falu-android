package io.falu.identity.navigation

internal class WelcomeDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val WELCOME = "welcome"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = WELCOME
        }
    }
}