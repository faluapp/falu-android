package io.falu.identity.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.falu.identity.scan.ScanDisposition

internal class ScanTimeoutDestination(scanType: ScanDisposition.DocumentScanType?) : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE
    override val routeWithArgs: String = workflowRoute.withParameters(
        KEY_SCAN_TYPE to scanType
    )

    internal companion object {
        const val SCAN_TIMEOUT = "scan_timeout"
        internal const val KEY_SCAN_TYPE = "scan_type"

        internal fun scanType(entry: NavBackStackEntry) =
            entry.getSerializable<ScanDisposition.DocumentScanType>(KEY_SCAN_TYPE)

        val ROUTE = object : WorkflowRoute() {
            override val base: String = SCAN_TIMEOUT
            override val arguments = listOf(
                navArgument(KEY_SCAN_TYPE) {
                    type = NavType.EnumType(ScanDisposition.DocumentScanType::class.java)
                }
            )
        }
    }
}