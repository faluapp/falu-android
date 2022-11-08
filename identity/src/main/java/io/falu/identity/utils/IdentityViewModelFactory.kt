@file:Suppress("UNCHECKED_CAST")

package io.falu.identity.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal fun createFactoryFor(viewModel: ViewModel) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModel as T
    }
}