package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.android.outlinevpntv.data.preferences.PreferencesManager
import app.android.outlinevpntv.data.remote.IpCountryCodeProvider
import app.android.outlinevpntv.data.remote.RemoteJSONFetch
import app.android.outlinevpntv.data.remote.ServerIconProvider

class ServerItemViewModel(private val serverIconProvider: ServerIconProvider) : ViewModel() {

    private val _serverIconState = MutableLiveData<String>()
    val serverIconState: LiveData<String> get() = _serverIconState

    suspend fun serverHost(serverHost: String) {
        _serverIconState.value = serverIconProvider.icon(serverHost)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[APPLICATION_KEY]!!.applicationContext
                ServerItemViewModel(
                    serverIconProvider = ServerIconProvider.FlagsApiDotCom(
                        ipCountryCodeProvider = IpCountryCodeProvider.IpApiDotCo(
                            fetch = RemoteJSONFetch.HttpURLConnectionJSONFetch()
                        ),
                        preferencesManager = PreferencesManager(context = context),
                    )
                )
            }
        }
    }
}