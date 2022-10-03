package io.falu.identity

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.identity.api.IdentityVerificationApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * View model that is shared across all fragments
 */
internal class IdentityVerificationViewModel(private val apiClient: IdentityVerificationApiClient) :
    ViewModel(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    internal companion object {
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