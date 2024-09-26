package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.android.outlinevpntv.data.remote.ServerIconProvider

class ServerItemViewModel(private val serverIconProvider: ServerIconProvider) : ViewModel() {

    private val _serverIconState = MutableLiveData<String>()
    val serverIconState: LiveData<String> get() = _serverIconState

    suspend fun serverHost(serverHost: String) {
        _serverIconState.value = serverIconProvider.icon(serverHost)
    }

    class Factory(
        private val serverIconProvider: ServerIconProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ServerItemViewModel(serverIconProvider) as T
        }
    }
}