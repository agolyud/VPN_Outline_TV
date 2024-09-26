package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.android.outlinevpntv.data.preferences.PreferencesManager
import app.android.outlinevpntv.data.remote.ParseUrlOutline
import app.android.outlinevpntv.domain.OutlineVpnManager
import app.android.outlinevpntv.viewmodel.state.SingleLiveEvent
import app.android.outlinevpntv.viewmodel.state.VpnEvent
import app.android.outlinevpntv.viewmodel.state.VpnServerStateUi
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferencesManager: PreferencesManager,
    private val vpnManager: OutlineVpnManager,
    private val parseUrlOutline: ParseUrlOutline,
) : ViewModel() {

    private val _vpnServerState = MutableLiveData<VpnServerStateUi>()
    val vpnServerState: LiveData<VpnServerStateUi> get() = _vpnServerState

    private val _vpnConnectionState = MutableLiveData<Boolean>()
    val vpnConnectionState: LiveData<Boolean> get() = _vpnConnectionState

    private val _errorEvent = SingleLiveEvent<Unit>()
    val errorEvent: LiveData<Unit> get() = _errorEvent

    fun startVpn(configString: String) {
        viewModelScope.launch {
            runCatching { parseUrlOutline.parse(configString) }
                .onSuccess { config -> vpnManager.start(config) }
                .onFailure { errorVpnEvent() }
        }
    }

    fun stopVpn() {
        vpnManager.stop()
    }

    fun checkVpnConnectionState() {
        val isVpnConnected = vpnManager.isConnected()

        _vpnConnectionState.value = isVpnConnected
    }

    fun loadLastVpnServerState() {
        val startTime = preferencesManager.getVpnStartTime()
        val name = preferencesManager.getServerName() ?: ""
        val url = preferencesManager.getVpnKey() ?: ""
        val host = runCatching { parseUrlOutline.extractServerHost(url) ?: "" }
            .getOrDefault("")

        _vpnServerState.value = VpnServerStateUi(
            name = name,
            host = host,
            url = url,
            startTime = startTime
        )
    }

    fun saveVpnServer(name: String, url: String) {
        preferencesManager.saveServerName(name)
        preferencesManager.saveVpnKey(url)
        preferencesManager.clearVpnStartTime()

        val host = runCatching { parseUrlOutline.extractServerHost(url) ?: "" }.getOrDefault("")

        _vpnServerState.value = VpnServerStateUi(name = name, host = host, url = url)
    }

    fun vpnEvent(event: VpnEvent) {
        when(event) {
            VpnEvent.STARTED -> {
                val started = System.currentTimeMillis()
                preferencesManager.saveVpnStartTime(started)
                _vpnServerState.value = _vpnServerState.value?.copy(startTime = started)
                _vpnConnectionState.value = true
            }
            VpnEvent.STOPPED -> {
                preferencesManager.clearVpnStartTime()
                _vpnServerState.value = _vpnServerState.value?.copy(startTime = 0L)
                _vpnConnectionState.value = false
            }

            VpnEvent.ERROR -> {
                preferencesManager.clearVpnStartTime()
                errorVpnEvent()
            }
        }
    }

    private fun errorVpnEvent() {
        _errorEvent.value = Unit
        checkVpnConnectionState()
    }

    class Factory(
        private val preferencesManager: PreferencesManager,
        private val vpnManager: OutlineVpnManager,
        private val parseUrlOutline: ParseUrlOutline,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(preferencesManager, vpnManager, parseUrlOutline) as T
        }
    }
}