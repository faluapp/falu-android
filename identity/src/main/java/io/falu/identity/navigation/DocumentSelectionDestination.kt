package io.falu.identity.navigation

internal class DocumentSelectionDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    internal companion object {
        const val DOCUMENT_SELECTION = "document_selection"

        val ROUTE = object : WorkflowRoute() {
            override val base: String = DOCUMENT_SELECTION
        }
    }
}