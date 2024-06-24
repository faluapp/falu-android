package io.falu.identity

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.core.AnalyticsApiClient
import io.falu.core.models.AnalyticsTelemetry
import io.falu.core.models.FaluFile
import io.falu.core.utils.toThrowable
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.FilesApiClient
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.api.models.verification.VerificationUploadResult
import io.falu.identity.utils.FileUtils
import io.falu.identity.utils.toWholeNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.ResourceResponse
import java.io.File
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * View model that is shared across all fragments
 */
internal class IdentityVerificationViewModel(
    internal val apiClient: IdentityVerificationApiClient,
    internal val analyticsRequestBuilder: IdentityAnalyticsRequestBuilder,
    internal val contractArgs: ContractArgs,
    private val fileUtils: FileUtils
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val filesApiClient = FilesApiClient()
    private val analyticsApiClient = AnalyticsApiClient(contractArgs.temporaryKey)

    internal val modelPerformanceMonitor = ModelPerformanceMonitor(analyticsRequestBuilder, analyticsApiClient)

    /**
     *
     */
    private val disposition: DocumentUploadDisposition = DocumentUploadDisposition()
    private val _documentUploadDisposition = MutableStateFlow(disposition)
    val documentUploadDisposition: LiveData<DocumentUploadDisposition>
        get() = _documentUploadDisposition.asLiveData(Dispatchers.Main)

    /**
     * Disposition to track the status of analytics
     */
    private val analyticsTelemetryDisposition: AnalyticsDisposition = AnalyticsDisposition()
    private val _analyticsDisposition = MutableStateFlow(analyticsTelemetryDisposition)
    val analyticsDisposition: StateFlow<AnalyticsDisposition> = _analyticsDisposition

    /**
     *
     */
    private val _verification = MutableLiveData<ResourceResponse<Verification>?>()
    val verification: LiveData<ResourceResponse<Verification>?>
        get() = _verification

    /**
     *
     */
    private val _supportedCountries = MutableLiveData<ResourceResponse<Array<SupportedCountry>>?>()
    private val supportedCountries: LiveData<ResourceResponse<Array<SupportedCountry>>?>
        get() = _supportedCountries

    /**
     *
     */
    private val _documentDetectorModelFile = MutableLiveData<File?>()
    val documentDetectorModelFile: LiveData<File?>
        get() = _documentDetectorModelFile

    /**
     *
     */
    private val _faceDetectorModelFile = MutableLiveData<File?>()
    val faceDetectorModelFile: LiveData<File?>
        get() = _faceDetectorModelFile

    private fun Uri.isHttp() = this.scheme!!.startsWith("http")

    internal fun fetchVerification(modelRequired: Boolean = true, onFailure: (Throwable) -> Unit) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.getVerification(contractArgs.verificationId)
            }.fold(
                onSuccess = {
                    _verification.postValue(it)

                    if (it.successful() && it.resource != null) {
                        val verification = it.resource!!
                        if (modelRequired) {

                            downloadAIModel(
                                verification.capture.models.document.url,
                                _documentDetectorModelFile
                            )
                            verification.capture.models.face?.let { face ->
                                downloadAIModel(face.url, _faceDetectorModelFile)
                            }
                        }
                    }
                },
                onFailure = {
                    Log.e(TAG, "Error getting verification", it)
                    handleFailureResponse(it, onFailure = onFailure)
                }
            )
        }
    }

    internal fun uploadVerificationDocument(
        bitmap: Bitmap,
        documentSide: DocumentSide,
        type: UploadMethod,
        onError: (Throwable?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        uploadFile(
            file = fileUtils.createFileFromBitmap(
                bitmap = bitmap,
                contractArgs.verificationId,
                documentSide.code
            ),
            documentSide = documentSide,
            type = type,
            verification = contractArgs.verificationId,
            onError = onError,
            onFailure = onFailure
        )
    }

    internal fun uploadSelfieImage(
        bitmap: Bitmap,
        onSuccess: (FaluFile) -> Unit,
        onError: (Throwable?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.uploadIdentityDocuments(
                    verification = contractArgs.verificationId,
                    purpose = "identity_private",
                    file = fileUtils.createFileFromBitmap(
                        bitmap,
                        contractArgs.verificationId,
                        "face"
                    )
                )
            }.fold(
                onSuccess = { response ->
                    handleResponse(
                        response,
                        onSuccess = { onSuccess(it) },
                        onError = { onError(it) })
                },
                onFailure = {
                    Log.e(TAG, "Error uploading selfie image", it)
                    handleFailureResponse(it, onFailure = onFailure)
                }
            )
        }
    }

    /**
     * Upload image from the scan.
     */
    internal fun uploadScannedDocument(
        bitmap: Bitmap,
        documentSide: DocumentSide,
        score: Float,
        onError: (Throwable?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        uploadFile(
            file = fileUtils.createFileFromBitmap(
                bitmap = bitmap,
                contractArgs.verificationId,
                documentSide.code
            ),
            documentSide = documentSide,
            type = UploadMethod.AUTO,
            verification = contractArgs.verificationId,
            score = score,
            onError = onError,
            onFailure = onFailure
        )
    }

    private fun uploadFile(
        file: File,
        documentSide: DocumentSide,
        type: UploadMethod,
        verification: String,
        score: Float? = null,
        onError: (Throwable?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.uploadIdentityDocuments(
                    verification = verification,
                    purpose = "identity.private",
                    file = file
                )
            }.fold(
                onSuccess = { response ->
                    if (response.successful() && response.resource != null) {
                        val result = VerificationUploadResult(
                            response.resource!!,
                            score?.toWholeNumber(),
                            type
                        )
                        _documentUploadDisposition.update { current ->
                            current.modify(documentSide, result)
                        }
                    } else {
                        handleResponse(response, onError = { onError(it) })
                    }
                },
                onFailure = {
                    Log.e(TAG, "Error uploading verification document", it)
                    handleFailureResponse(it, onFailure = onFailure)
                }
            )
        }
    }

    internal fun updateVerification(
        updateOptions: VerificationUpdateOptions,
        onSuccess: ((Verification) -> Unit),
        onError: ((Throwable?) -> Unit),
        onFailure: ((Throwable) -> Unit)
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.updateVerification(contractArgs.verificationId, updateOptions)
            }.fold(
                onSuccess = { response ->
                    handleResponse(
                        response,
                        onSuccess = { onSuccess(it) },
                        onError = { onError(it) })
                },
                onFailure = {
                    Log.e(TAG, "Error updating verification", it)
                    handleFailureResponse(it, onFailure = onFailure)
                }
            )
        }
    }

    internal fun submitVerificationData(
        uploadRequest: VerificationUploadRequest,
        onSuccess: ((Verification) -> Unit),
        onError: ((Throwable?) -> Unit),
        onFailure: (Throwable) -> Unit
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.submitVerificationDocuments(contractArgs.verificationId, uploadRequest)
            }.fold(
                onSuccess = { response ->
                    handleResponse(
                        response,
                        onSuccess = onSuccess,
                        onError = { onError(it) })
                },
                onFailure = {
                    Log.e(TAG, "Error submitting verification", it)
                    handleFailureResponse(it, onFailure = onFailure)
                }
            )
        }
    }

    internal fun fetchSupportedCountries() {
        launch(Dispatchers.IO) {
            runCatching {
                filesApiClient.getSupportedCountries()
            }.fold(
                onSuccess = {
                    _supportedCountries.postValue(it)
                },
                onFailure = {
                    Log.e(TAG, "Error getting verification", it)
                }
            )
        }
    }

    internal fun loadUriToImageView(uri: Uri, view: ImageView) {
        if (uri.isHttp()) {
            downloadFile(uri) {
                view.setImageURI(fileUtils.getFileUri(it))
            }
        } else {
            view.setImageURI(uri)
        }
    }

    private fun downloadFile(
        uri: Uri,
        success: ((File) -> Unit)
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                fileUtils.imageFile.let {
                    filesApiClient.downloadFile(uri.toString(), it)
                }
            }.fold(
                onSuccess = {
                    handleResponse(it, onSuccess = { file -> success(file) }, onError = {})
                },
                onFailure = {
                    Log.e(TAG, "Error downloading file", it)
                }
            )
        }
    }

    private fun downloadAIModel(url: String, liveData: MutableLiveData<File?>) {
        fileUtils.createMLModelFile(url).let { file ->
            if (file.exists()) {
                liveData.postValue(file)
                return
            }
            downloadAIModel(url, file, liveData)
        }
    }

    private fun downloadAIModel(url: String, file: File, liveData: MutableLiveData<File?>) {
        launch(Dispatchers.IO) {
            runCatching {
                filesApiClient.downloadFile(url, file)
            }.fold(
                onSuccess = {
                    if (it.successful() && it.resource != null) {
                        liveData.postValue(it.resource)
                    }
                },
                onFailure = {
                    Log.e(TAG, "Error getting verification", it)
                }
            )
        }
    }

    internal fun resetDocumentUploadDisposition() {
        _documentUploadDisposition.update { DocumentUploadDisposition() }
    }

    internal fun modifyAnalyticsDisposition(disposition: AnalyticsDisposition) {
        _analyticsDisposition.update { current -> current.modify(disposition) }
    }

    fun observeForVerificationResults(
        owner: LifecycleOwner,
        onSuccess: ((Verification) -> Unit),
        onError: ((Throwable?) -> Unit)
    ) {
        verification.observe(owner) { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess(response.resource!!)
            } else {
                onError(response?.toThrowable())
            }
        }
    }

    fun observerForSupportedCountriesResults(
        owner: LifecycleOwner,
        onSuccess: ((Array<SupportedCountry>) -> Unit),
        onError: ((HttpApiResponseProblem?) -> Unit)
    ) {
        supportedCountries.observe(owner) { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess(response.resource!!)
            } else {
                onError(response?.error)
            }
        }
    }

    fun reportTelemetry(telemetry: AnalyticsTelemetry) {
        launch(Dispatchers.IO) {
            analyticsApiClient.reportTelemetry(telemetry, IdentityAnalyticsRequestBuilder.ORIGIN)
        }
    }

    fun reportSuccessfulVerificationTelemetry() {

        launch(Dispatchers.IO) {
            analyticsDisposition.collectLatest { latest ->
                val cake = analyticsRequestBuilder.verificationSuccessful(
                    fromFallbackUrl = false,
                    backModelScore = latest.backModelScore,
                    uploadMethod = latest.uploadMethod,
                    frontModelScore = latest.frontModelScore,
                    scanType = latest.scanType,
                    selfieModelScore = latest.selfieModelScore,
                    selfie = latest.selfie
                )

                analyticsApiClient.reportTelemetry(cake, IdentityAnalyticsRequestBuilder.ORIGIN)
            }
        }
    }

    private suspend fun <TResource> handleResponse(
        response: ResourceResponse<TResource>?,
        onSuccess: ((TResource) -> Unit)? = null,
        onError: ((Throwable?) -> Unit)
    ) = withContext(Dispatchers.Main) {
        if (response != null && response.successful() && response.resource != null) {
            onSuccess?.invoke(response.resource!!)
            return@withContext
        }

        onError(response?.toThrowable())
    }

    fun getModel(stream: InputStream, fileName: String): File {
        return fileUtils.createFileFromInputStream(stream, fileName)
    }

    private suspend fun handleFailureResponse(
        throwable: Throwable,
        onFailure: (Throwable) -> Unit
    ) = withContext(Dispatchers.Main) {
        onFailure(throwable)
    }

    internal companion object {
        private val TAG = IdentityVerificationViewModel::class.java.simpleName

        fun factoryProvider(
            savedStateRegistryOwner: SavedStateRegistryOwner,
            apiClient: () -> IdentityVerificationApiClient,
            analyticsRequestBuilder: () -> IdentityAnalyticsRequestBuilder,
            fileUtils: () -> FileUtils,
            contractArgs: () -> ContractArgs
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return IdentityVerificationViewModel(
                        apiClient(),
                        analyticsRequestBuilder(),
                        contractArgs(),
                        fileUtils()
                    ) as T
                }
            }
    }
}