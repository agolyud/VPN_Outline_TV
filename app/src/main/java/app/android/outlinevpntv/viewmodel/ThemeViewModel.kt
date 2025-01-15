package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.android.outlinevpntv.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(preferencesManager.getSelectedTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            // Сохраняем в SharedPreferences (или DataStore)
            preferencesManager.saveSelectedTheme(isDark)
            // Обновляем стейт
            _isDarkTheme.value = isDark
        }
    }
}


