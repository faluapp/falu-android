package io.falu.identity

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.core.models.FaluFile
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.FilesApiClient
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.api.models.verification.VerificationUploadResult
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.ResourceResponse
import software.tingle.api.patch.JsonPatchDocument
import java.io.File
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * View model that is shared across all fragments
 */
internal class IdentityVerificationViewModel(
    internal val apiClient: IdentityVerificationApiClient,
    internal val contractArgs: ContractArgs,
    private val fileUtils: FileUtils
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val filesApiClient = FilesApiClient()

    /**
     *
     */
    private val disposition: DocumentUploadDisposition = DocumentUploadDisposition()
    private val _documentUploadDisposition = MutableStateFlow(disposition)
    val documentUploadDisposition: LiveData<DocumentUploadDisposition>
        get() = _documentUploadDisposition.asLiveData(Dispatchers.Main)

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
    private val _documentDetectorModelFile = MutableLiveData<ResourceResponse<File>?>()
    private val documentDetectorModelFile: LiveData<ResourceResponse<File>?>
        get() = _documentDetectorModelFile

    /**
     *
     */
    private val _documentTypeDetectorModelFile = MutableLiveData<ResourceResponse<File>?>()
    private val documentTypeDetectorModelFile: LiveData<ResourceResponse<File>?>
        get() = _documentTypeDetectorModelFile

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

                            downloadAIModel(
                                verification.capture.models.documentType.url,
                                _documentDetectorModelFile
                            )
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
        uri: Uri,
        documentSide: DocumentSide,
        type: UploadMethod,
        onError: (HttpApiResponseProblem?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        uploadFile(
            file = fileUtils.createFileFromUri(
                fileUri = uri,
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
        uri: Uri,
        onSuccess: (FaluFile) -> Unit,
        onError: (HttpApiResponseProblem?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.uploadIdentityDocuments(
                    verification = contractArgs.verificationId,
                    purpose = "identity_private",
                    file = fileUtils.createFileFromUri(uri, contractArgs.verificationId)
                )
            }.fold(
                onSuccess = { response ->
                    handleResponse(
                        response,
                        onSuccess = { onSuccess(it) },
                        onError = { onError(it?.error) })
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
        verification: String,
        scanType: DocumentScanDisposition.DocumentScanType,
        onError: (HttpApiResponseProblem?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val documentSide = when (scanType) {
            DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_FRONT,
            DocumentScanDisposition.DocumentScanType.DL_FRONT,
            DocumentScanDisposition.DocumentScanType.PASSPORT -> DocumentSide.FRONT
            DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_BACK,
            DocumentScanDisposition.DocumentScanType.DL_BACK -> DocumentSide.BACK
        }

        uploadFile(
            file = fileUtils.createFileFromBitmap(
                bitmap = bitmap,
                verification,
                documentSide.code
            ),
            documentSide = documentSide,
            type = UploadMethod.AUTO,
            verification = contractArgs.verificationId,
            onError = onError,
            onFailure = onFailure
        )
    }

    private fun uploadFile(
        file: File,
        documentSide: DocumentSide,
        type: UploadMethod,
        verification: String,
        onError: (HttpApiResponseProblem?) -> Unit,
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
                        val result = VerificationUploadResult(response.resource!!, type)
                        _documentUploadDisposition.update { current ->
                            current.modify(documentSide, result)
                        }
                    } else {
                        handleResponse(response, onError = { onError(it?.error) })
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
        document: JsonPatchDocument,
        onSuccess: ((Verification) -> Unit),
        onError: ((HttpApiResponseProblem?) -> Unit),
        onFailure: ((Throwable) -> Unit)
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.updateVerification(contractArgs.verificationId, document)
            }.fold(
                onSuccess = { response ->
                    handleResponse(
                        response,
                        onSuccess = { onSuccess(it) },
                        onError = { onError(it?.error) })
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
        onError: ((HttpApiResponseProblem?) -> Unit),
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
                        onError = { onError(it?.error) })
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

    private fun downloadAIModel(url: String, liveData: MutableLiveData<ResourceResponse<File>?>) {
        launch(Dispatchers.IO) {
            runCatching {
                filesApiClient.downloadModelFile(url)
            }.fold(
                onSuccess = {
                    liveData.postValue(it)
                },
                onFailure = {
                    Log.e(TAG, "Error getting verification", it)
                }
            )
        }
    }

    fun observeForVerificationResults(
        owner: LifecycleOwner,
        onSuccess: ((Verification) -> Unit),
        onError: ((HttpApiResponseProblem?) -> Unit)
    ) {
        verification.observe(owner, Observer<ResourceResponse<Verification>?> { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess(response.resource!!)
            } else {
                onError(response?.error)
            }
        })
    }

    fun observerForSupportedCountriesResults(
        owner: LifecycleOwner,
        onSuccess: ((Array<SupportedCountry>) -> Unit),
        onError: ((HttpApiResponseProblem?) -> Unit)
    ) {
        supportedCountries.observe(
            owner,
            Observer<ResourceResponse<Array<SupportedCountry>>?> { response ->
                if (response != null && response.successful() && response.resource != null) {
                    onSuccess(response.resource!!)
                } else {
                    onError(response?.error)
                }
            })
    }

    private suspend fun <TResource> handleResponse(
        response: ResourceResponse<TResource>?,
        onSuccess: ((TResource) -> Unit)? = null,
        onError: ((ResourceResponse<TResource>?) -> Unit)
    ) = withContext(Dispatchers.Main) {
        if (response != null && response.successful() && response.resource != null) {
            onSuccess?.invoke(response.resource!!)
            return@withContext
        }
        onError(response)
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
            fileUtils: () -> FileUtils,
            contractArgs: () -> ContractArgs,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return IdentityVerificationViewModel(
                        apiClient(),
                        contractArgs(),
                        fileUtils()
                    ) as T
                }
            }
    }
}