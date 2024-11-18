package io.falu.identity.navigation

internal class CameraPermissionDeniedDestination : IdentityDestination() {
    override val workflowRoute: WorkflowRoute
        get() = ROUTE

    companion object {
        const val CAMERA_PERMISSION_DENIED = "CameraPermissionDenied"

        val ROUTE = object : WorkflowRoute() {
            override val base = CAMERA_PERMISSION_DENIED
        }
    }
}