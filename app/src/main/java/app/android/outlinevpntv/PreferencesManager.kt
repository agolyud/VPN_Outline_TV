package app.android.outlinevpntv

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveVpnKey(key: String) {
        preferences.edit().putString(KEY_VPN, key).apply()
    }

    fun getVpnKey(): String? {
        return preferences.getString(KEY_VPN, null)
    }

    companion object {
        private const val PREFS_NAME = "outline_vpn_prefs"
        private const val KEY_VPN = "vpn_key"
    }
}
