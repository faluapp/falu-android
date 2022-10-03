package io.falu.identity

import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.identity.api.IdentityVerificationApiClient
import io.falu.identity.api.models.Verification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.ResourceResponse
import kotlin.coroutines.CoroutineContext

/**
 * View model that is shared across all fragments
 */
internal class IdentityVerificationViewModel(private val apiClient: IdentityVerificationApiClient) :
    ViewModel(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val verification = MutableLiveData<ResourceResponse<Verification>>()
    val verificationPage: LiveData<ResourceResponse<Verification>> = verification

    fun fetchVerification() {
        launch(Dispatchers.IO) {
            runCatching {
                apiClient.getVerification()
            }.fold(
                onSuccess = {
                    verification.postValue(it)
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
        verification.observe(owner) { response ->
            if (response != null && response.successful() && response.resource != null) {
                onSuccess(response.resource!!)
            } else {
                onFailure(response.error)
            }
        }
    }

    internal companion object {
        private val TAG = IdentityVerificationViewModel::class.java.simpleName

        fun factoryProvider(
            savedStateRegistryOwner: SavedStateRegistryOwner,
            apiClient: IdentityVerificationApiClient
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return IdentityVerificationViewModel(apiClient) as T
                }
            }
    }
}