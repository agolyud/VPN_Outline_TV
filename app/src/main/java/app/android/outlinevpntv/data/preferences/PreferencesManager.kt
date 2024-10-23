package app.android.outlinevpntv.data.preferences

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

    fun saveServerName(name: String) {
        preferences.edit().putString(KEY_SERVER_NAME, name).apply()
    }

    fun getServerName(): String? {
        return preferences.getString(KEY_SERVER_NAME, null)
    }

    fun saveFlagUrl(ip: String, flagUrl: String) {
        preferences.edit().putString("flag_$ip", flagUrl).apply()
    }

    fun getFlagUrl(ip: String): String? {
        return preferences.getString("flag_$ip", null)
    }


    fun saveSelectedDns(dns: String) {
        preferences.edit().putString(KEY_SELECTED_DNS, dns).apply()
    }

    fun getSelectedDns(): String? {
        return preferences.getString(KEY_SELECTED_DNS, null)
    }

    companion object {
        private const val PREFS_NAME = "outline_vpn_prefs"
        private const val KEY_VPN = "vpn_key"
        private const val KEY_VPN_START_TIME = "vpn_start_time"
        private const val KEY_SERVER_NAME = "server_name"
        private const val KEY_SELECTED_DNS = "selected_dns"
    }
}
