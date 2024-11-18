package io.falu.identity.navigation

internal class SelfieDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val SELFIE = "selfie"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = SELFIE
        }
    }
}