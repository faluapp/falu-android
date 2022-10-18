package io.falu.identity

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.identity.api.CountriesApiClient
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.UploadType
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.api.models.verification.VerificationUploadResult
import io.falu.identity.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.ResourceResponse
import software.tingle.api.patch.JsonPatchDocument
import kotlin.coroutines.CoroutineContext

/**
 * View model that is shared across all fragments
 */
internal class IdentityVerificationViewModel(
    private val apiClient: IdentityVerificationApiClient,
    private val contractArgs: ContractArgs,
    private val fileUtils: FileUtils
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val countriesApiClient = CountriesApiClient()

    /**
     *
     */
    private val _documentUploadDisposition = MutableLiveData(
        DocumentUploadDisposition(front = null, back = null)
    )
    val documentUploadDisposition: LiveData<DocumentUploadDisposition>
        get() = _documentUploadDisposition

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

    internal fun fetchVerification() {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.getVerification(contractArgs.verificationId)
            }.fold(
                onSuccess = {
                    _verification.postValue(it)
                },
                onFailure = {
                    Log.e(TAG, "Error getting verification", it)
                }
            )
        }
    }

    internal fun uploadVerificationDocument(
        uri: Uri,
        documentSide: DocumentSide,
        type: UploadType,
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.uploadIdentityDocuments(
                    verification = contractArgs.verificationId,
                    purpose = "identity.private",
                    file = fileUtils.createFileFromUri(
                        fileUri = uri,
                        contractArgs.verificationId,
                        documentSide.code
                    )
                )
            }.fold(
                onSuccess = { response ->
                    if (response.successful() && response.resource != null) {
                        val result = VerificationUploadResult(response.resource!!, type)
                        _documentUploadDisposition.postValue(
                            DocumentUploadDisposition().modify(
                                documentSide,
                                result
                            )
                        )
                    } else {
                        // TODO: 2022-10-18 Handle errors
                    }
                },
                onFailure = {
                    Log.e(TAG, "Error uploading verification document", it)
                }
            )
        }
    }

    internal fun updateVerification(
        document: JsonPatchDocument,
        onSuccess: (() -> Unit),
        onFailure: ((HttpApiResponseProblem?) -> Unit)
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.updateVerification(contractArgs.verificationId, document)
            }.fold(
                onSuccess = { response ->
                    handleResponse(
                        response,
                        onSuccess = { onSuccess() },
                        onError = { onFailure(response.error) })
                },
                onFailure = {
                    Log.e(TAG, "Error updating verification", it)
                }
            )
        }
    }

    internal fun submitVerificationData(
        uploadRequest: VerificationUploadRequest,
        onSuccess: ((Verification) -> Unit),
        onFailure: ((HttpApiResponseProblem?) -> Unit)
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.submitVerificationDocuments(contractArgs.verificationId, uploadRequest)
            }.fold(
                onSuccess = { response ->
                },
                onFailure = {
                    Log.e(TAG, "Error submitting verification", it)
                }
            )
        }
    }

    internal fun fetchSupportedCountries() {
        launch(Dispatchers.IO) {
            runCatching {
                countriesApiClient.getSupportedCountries()
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

    fun observeForVerificationResults(
        owner: LifecycleOwner,
        onSuccess: ((Verification) -> Unit),
        onFailure: ((HttpApiResponseProblem?) -> Unit)
    ) {
        verification.observe(owner, Observer<ResourceResponse<Verification>?> { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess(response.resource!!)
            } else {
                onFailure(response?.error)
            }
        })
    }

    fun observerForSupportedCountriesResults(
        owner: LifecycleOwner,
        onSuccess: ((Array<SupportedCountry>) -> Unit),
        onFailure: ((HttpApiResponseProblem?) -> Unit)
    ) {
        supportedCountries.observe(
            owner,
            Observer<ResourceResponse<Array<SupportedCountry>>?> { response ->
                if (response != null && response.successful() && response.resource != null) {
                    onSuccess(response.resource!!)
                } else {
                    onFailure(response?.error)
                }
            })
    }

    private suspend fun <TResource> handleResponse(
        response: ResourceResponse<TResource>?,
        onSuccess: ((TResource) -> Unit),
        onError: ((ResourceResponse<TResource>?) -> Unit)
    ) = withContext(Dispatchers.Main) {
        if (response != null && response.successful() && response.resource != null) {
            onSuccess(response.resource!!)
            return@withContext
        }
        onError(response)
    }

    internal companion object {
        private val TAG = IdentityVerificationViewModel::class.java.simpleName

        fun factoryProvider(
            savedStateRegistryOwner: SavedStateRegistryOwner,
            apiClient: IdentityVerificationApiClient,
            fileUtils: FileUtils,
            contractArgs: ContractArgs,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return IdentityVerificationViewModel(apiClient, contractArgs, fileUtils) as T
                }
            }
    }
}