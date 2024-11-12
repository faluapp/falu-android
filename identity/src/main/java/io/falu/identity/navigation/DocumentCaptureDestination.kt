package io.falu.identity.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.falu.identity.api.models.IdentityDocumentType

internal data class DocumentCaptureDestination(val documentType: IdentityDocumentType) : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    override val routeWithArgs: String = workflowRoute.withParameters(
        KEY_DOCUMENT_TYPE to documentType
    )

    internal companion object {
        const val CAPTURE_METHODS = "document_capture_methods"
        internal const val KEY_DOCUMENT_TYPE = "document_type"

        internal fun identityDocumentType(entry: NavBackStackEntry) =
            entry.getSerializable<IdentityDocumentType>(KEY_DOCUMENT_TYPE)!!

        val ROUTE = object : WorkflowRoute() {
            override val base: String = CAPTURE_METHODS
            override val arguments = listOf(
                navArgument(KEY_DOCUMENT_TYPE) {
                    type = NavType.EnumType(IdentityDocumentType::class.java)
                }
            )
        }
    }
}