package app.android.outlinevpntv

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _vpnState = MutableLiveData<Boolean>()
    val vpnState: LiveData<Boolean> get() = _vpnState

    fun startVpn(context: Context) {
        try {
            OutlineVpnService.start(context)
            _vpnState.value = true
        } catch (e: Exception) {
            _vpnState.value = false
        }
    }

    fun stopVpn(context: Context) {
        OutlineVpnService.stop(context)
        _vpnState.value = false
    }
}
