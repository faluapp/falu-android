package io.falu.identity

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.core.models.FaluFile
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.Verification
import io.falu.identity.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.ResourceResponse
import kotlin.coroutines.CoroutineContext

/**
 * View model that is shared across all fragments
 */
internal class IdentityVerificationViewModel(
    private val apiClient: IdentityVerificationApiClient,
    private val contractArgs: ContractArgs,
    private val fileUtils: FileUtils
) :
    ViewModel(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val _verification = MutableLiveData<ResourceResponse<Verification>?>()
    val verification: LiveData<ResourceResponse<Verification>?>
        get() = _verification

    private val _documentUpload = MutableLiveData<ResourceResponse<FaluFile>?>()
    private val documentUpload: LiveData<ResourceResponse<FaluFile>?>
        get() = _documentUpload

    fun fetchVerification() {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.getVerification()
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

    internal fun uploadVerificationDocument(uri: Uri, documentSide: DocumentSide) {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.uploadIdentityDocuments(
                    verification = contractArgs.verificationId,
                    purpose = "identity.document",
                    documentSide = documentSide,
                    file = fileUtils.createFileFromUri(
                        fileUri = uri,
                        contractArgs.verificationId,
                        documentSide.code
                    )
                )
            }.fold(
                onSuccess = {
                    _documentUpload.postValue(it)
                },
                onFailure = {

                }
            )
        }
    }

    fun observeForVerificationResults(
        owner: LifecycleOwner,
        onSuccess: ((Verification) -> Unit),
        onFailure: ((HttpApiResponseProblem?) -> Unit)
    ) {
        verification.observe(owner) { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess(response.resource!!)
            } else {
                onFailure(response?.error)
            }
        }
    }

    fun observerForDocumentUploadResults(
        owner: LifecycleOwner,
        onSuccess: (() -> Unit),
        onFailure: ((HttpApiResponseProblem?) -> Unit)
    ) {
        documentUpload.observe(owner) { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess()
            } else {
                onFailure(response?.error)
            }
        }
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