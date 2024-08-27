package app.android.outlinevpntv

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager: PreferencesManager = PreferencesManager(application)

    private val _vpnState = MutableLiveData<Boolean>()
    val vpnState: LiveData<Boolean> get() = _vpnState

    fun startVpn(context: Context) {
        try {
            OutlineVpnService.start(context)
            val startTime = System.currentTimeMillis()
            preferencesManager.saveVpnStartTime(startTime)
            _vpnState.value = true
            waitForVpnConnection(context)
        } catch (e: Exception) {
            _vpnState.value = false
            throw e
        }
    }

    fun stopVpn(context: Context) {
        OutlineVpnService.stop(context)
        preferencesManager.clearVpnStartTime()
        _vpnState.value = false
        waitForVpnDisconnection()
    }

    fun getVpnStartTime(): Long {
        return preferencesManager.getVpnStartTime()
    }

    fun setVpnState(isConnected: Boolean) {
        _vpnState.value = isConnected
    }


    private fun waitForVpnConnection(context: Context) {
        viewModelScope.launch {
            delay(2000)
            while (true) {
                val isVpnConnected = OutlineVpnService.isVpnConnected()
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
                _vpnState.postValue(isVpnConnected)
                if (!isVpnConnected) break
                delay(1000)
            }
        }
    }
}





