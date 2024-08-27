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

    fun saveVpnStartTime(startTime: Long) {
        preferences.edit().putLong(KEY_VPN_START_TIME, startTime).apply()
    }

    fun getVpnStartTime(): Long {
        return preferences.getLong(KEY_VPN_START_TIME, 0L)
    }

    fun clearVpnStartTime() {
        preferences.edit().remove(KEY_VPN_START_TIME).apply()
    }

    companion object {
        private const val PREFS_NAME = "outline_vpn_prefs"
        private const val KEY_VPN = "vpn_key"
        private const val KEY_VPN_START_TIME = "vpn_start_time"
    }
}
