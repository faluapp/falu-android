package io.falu.identity.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.falu.identity.api.models.IdentityDocumentType

internal class DocumentVerificationDestination(identityDocumentType: IdentityDocumentType) : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    override val routeWithArgs: String = workflowRoute.withParameters(
        KEY_DOCUMENT_TYPE to identityDocumentType
    )

    internal companion object {
        const val DOCUMENT_VERIFICATION = "document_verification"
        const val KEY_DOCUMENT_TYPE = "document_type"

        internal fun identityDocumentType(entry: NavBackStackEntry) =
            entry.getSerializable<IdentityDocumentType>(KEY_DOCUMENT_TYPE)!!

        val ROUTE = object : WorkflowRoute() {
            override val base: String = DOCUMENT_VERIFICATION
            override val arguments = listOf(
                navArgument(KEY_DOCUMENT_TYPE) {
                    type = NavType.EnumType(IdentityDocumentType::class.java)
                }
            )
        }
    }
}