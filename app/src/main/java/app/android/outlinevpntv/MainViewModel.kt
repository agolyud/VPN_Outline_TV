package app.android.outlinevpntv

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _vpnState = MutableLiveData<Boolean>()
    val vpnState: LiveData<Boolean> get() = _vpnState

    fun startVpn(context: Context) {
        try {
            OutlineVpnService.start(context)
            waitForVpnConnection(context)
        } catch (e: Exception) {
            _vpnState.value = false
            throw e
        }
    }

    fun setVpnState(isConnected: Boolean) {
        _vpnState.value = isConnected
    }

    fun stopVpn(context: Context) {
        OutlineVpnService.stop(context)
        waitForVpnDisconnection()
    }

    private fun waitForVpnConnection(context: Context) {
        viewModelScope.launch {
            delay(2000)
            while (true) {
                val isVpnConnected = OutlineVpnService.isVpnConnected()
                Log.d("MainViewModel", "VPN connected: $isVpnConnected")
                _vpnState.postValue(isVpnConnected)
                if (isVpnConnected) break
                delay(1000)
            }
        }
    }

    private fun waitForVpnDisconnection() {
        viewModelScope.launch {
            delay(2000)
            while (true) {
                val isVpnConnected = OutlineVpnService.isVpnConnected()
                Log.d("MainViewModel", "VPN connected: $isVpnConnected")
                _vpnState.postValue(isVpnConnected)
                if (!isVpnConnected) break
                delay(1000)
            }
        }
    }
}





