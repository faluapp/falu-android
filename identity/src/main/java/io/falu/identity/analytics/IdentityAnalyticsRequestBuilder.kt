package io.falu.identity.analytics

import android.content.Context
import io.falu.core.AnalyticsRequestBuilder
import io.falu.identity.ContractArgs
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.scan.ScanDisposition

internal class IdentityAnalyticsRequestBuilder(
    context: Context,
    private val args: ContractArgs
) : AnalyticsRequestBuilder(context, CLIENT, ORIGIN) {

    var verification: Verification? = null

    fun viewOpened() = createRequest(EVENT_VERIFICATION_VIEW_OPENED, mapOf(KEY_VERIFICATION to args.verificationId))

    private fun makeEventParameters(vararg params: Pair<String, *>) = mapOf(
        KEY_VERIFICATION to args.verificationId,
        *params.also {
            verification?.let {
                KEY_LIVE to "${it.live}"
            }
        }
    )

    fun viewClosed(verificationResult: String) = createRequest(
        EVENT_VERIFICATION_VIEW_CLOSED, mapOf(
            EVENT_VERIFICATION_VIEW_CLOSED to verificationResult
        )
    )

    fun verificationSuccessful(
        fromFallbackUrl: Boolean,
        backModelScore: Float? = null,
        uploadMethod: UploadMethod? = null,
        frontModelScore: Float? = null,
        scanType: ScanDisposition.DocumentScanType? = null,
        selfieModelScore: Float? = null,
        selfie: Boolean? = null,
    ) = createRequest(
        EVENT_VERIFICATION_SUCCESSFUL, makeEventParameters(
            KEY_FROM_FALLBACK_URL to fromFallbackUrl.toString(),
            KEY_DOCUMENT_SCAN_TYPE to scanType?.name,
            KEY_SELFIE_REQUIRED to selfie.toString(),
            KEY_UPLOAD_METHOD to uploadMethod?.name,
            KEY_DOC_FRONT_MODEL_SCORE to frontModelScore.toString(),
            KEY_DOC_BACK_MODEL_SCORE to backModelScore.toString(),
            KEY_SELFIE_MODEL_SCORE to selfieModelScore.toString(),
        )
    )

    fun verificationCanceled(
        fromFallbackUrl: Boolean,
        scanType: ScanDisposition.DocumentScanType? = null,
        selfie: Boolean? = null,
        previousScreenName: String? = null
    ) = createRequest(
        EVENT_VERIFICATION_CANCELED, makeEventParameters(
            KEY_FROM_FALLBACK_URL to fromFallbackUrl.toString(),
            KEY_DOCUMENT_SCAN_TYPE to scanType?.name,
            KEY_SELFIE_REQUIRED to selfie.toString(),
            KEY_PREVIOUS_SCREEN to previousScreenName.toString()
        )
    )

    fun verificationFailed(
        fromFallbackUrl: Boolean,
        scanType: ScanDisposition.DocumentScanType? = null,
        selfie: Boolean? = null,
        throwable: Throwable?
    ) = createRequest(
        EVENT_VERIFICATION_FAILED, makeEventParameters(
            KEY_FROM_FALLBACK_URL to fromFallbackUrl.toString(),
            KEY_DOCUMENT_SCAN_TYPE to scanType?.name,
            KEY_SELFIE_REQUIRED to selfie.toString(),
            KEY_EXCEPTION to mapOf(
                KEY_EXCEPTION_NAME to throwable?.javaClass?.name,
                KEY_EXCEPTION_STACKTRACE to throwable?.stackTrace
            )
        )
    )

    fun documentScanTimeOut(scanType: ScanDisposition.DocumentScanType) = createRequest(
        EVENT_IDENTITY_DOCUMENT_TIMEOUT, mapOf(KEY_DOCUMENT_SCAN_TYPE to scanType.name)
    )

    fun cameraPermissionDenied(
        documentType: IdentityDocumentType
    ) = createRequest(
        EVENT_CAMERA_PERMISSION_DENIED,
        params = makeEventParameters(
            KEY_DOCUMENT_TYPE to documentType.name
        )
    )

    fun cameraPermissionGranted(
        documentType: IdentityDocumentType
    ) = createRequest(
        event = EVENT_CAMERA_PERMISSION_GRANTED,
        params = makeEventParameters(
            KEY_DOCUMENT_TYPE to documentType.name
        )
    )

    fun selfieScanTimeOut() = createRequest(EVENT_SELFIE_TIMEOUT)

    companion object {
        const val CLIENT = "identity-sdk-mobile"
        const val ORIGIN = "falu-identity-android"

        const val EVENT_VERIFICATION_VIEW_OPENED = "view_opened"
        const val EVENT_VERIFICATION_VIEW_CLOSED = "view_closed"
        const val EVENT_VERIFICATION_SUCCESSFUL = "verification_successful"
        const val EVENT_VERIFICATION_FAILED = "verification_failed"
        const val EVENT_VERIFICATION_CANCELED = "verification_canceled"
        const val EVENT_SCREEN_PRESENTED = "screen_presented"
        const val EVENT_CAMERA_ERROR = "camera_error"
        const val EVENT_CAMERA_PERMISSION_DENIED = "camera_permission_denied"
        const val EVENT_CAMERA_PERMISSION_GRANTED = "camera_permission_granted"
        const val EVENT_IDENTITY_DOCUMENT_TIMEOUT = "document_timeout"
        const val EVENT_SELFIE_TIMEOUT = "selfie_timeout"
        const val EVENT_AVERAGE_FPS = "average_fps"
        const val EVENT_MODEL_PERFORMANCE = "model_performance"
        const val EVENT_TIME_TO_SCREEN = "time_to_screen"
        const val EVENT_IMAGE_UPLOAD = "image_upload"
        const val EVENT_ERROR = "error"

        const val KEY_VERIFICATION = "verification"
        const val KEY_DOCUMENT_SCAN_TYPE = "scan_type"
        const val KEY_LIVE = "live"
        const val KEY_META_DATA = "metadata"
        const val KEY_SELFIE_REQUIRED = "selfie_required"
        const val KEY_PREVIOUS_SCREEN = "previous_screen_name"
        const val KEY_FROM_FALLBACK_URL = "from_fallback_url"
        const val KEY_EXCEPTION = "exception"
        const val KEY_EXCEPTION_NAME = "exception_name"
        const val KEY_EXCEPTION_STACKTRACE = "stacktrace"
        const val KEY_DOCUMENT_TYPE = "document_type"
        const val KEY_UPLOAD_METHOD = "upload_method"
        const val KEY_DOC_FRONT_MODEL_SCORE = "document_front_model_score"
        const val KEY_DOC_BACK_MODEL_SCORE = "document_back_model_score"
        const val KEY_SELFIE_MODEL_SCORE = "document_selfie_model_score"
    }
}