package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.ViewModel
import app.android.outlinevpntv.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoConnectViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    private val _isAutoConnectEnabled = MutableStateFlow(preferencesManager.isAutoConnectionEnabled())
    val isAutoConnectEnabled = _isAutoConnectEnabled.asStateFlow()

    fun setAutoConnectEnabled(enabled: Boolean) {
        preferencesManager.setAutoConnectionEnabled(enabled)
        _isAutoConnectEnabled.value = enabled
    }
}
