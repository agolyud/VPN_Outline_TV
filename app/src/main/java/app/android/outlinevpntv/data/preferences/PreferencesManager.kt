package app.android.outlinevpntv.data.preferences

import android.content.Context
import android.content.SharedPreferences
import app.android.outlinevpntv.data.model.VpnServerInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class PreferencesManager(context: Context) {

    private val gson = Gson()
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveVpnKeys(keys: List<VpnServerInfo>) {
        val json = gson.toJson(keys)
        preferences.edit().putString(KEY_VPN_LIST, json).apply()
    }

    fun getVpnKeys(): List<VpnServerInfo> {
        val json = preferences.getString(KEY_VPN_LIST, null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<VpnServerInfo>>() {}.type
            gson.fromJson(json, type)
        }
    }

    fun saveSelectedTheme(isDark: Boolean) {
        preferences.edit().putBoolean(KEY_SELECTED_THEME, isDark).apply()
    }

    fun getSelectedTheme(): Boolean {
        return preferences.getBoolean(KEY_SELECTED_THEME, false)
    }


    fun addOrUpdateVpnKey(serverName: String, key: String) {
        val existingList = getVpnKeys().toMutableList()
        val index = existingList.indexOfFirst { it.name == serverName }
        if (index >= 0) {
            existingList[index] = VpnServerInfo(name = serverName, key = key)
        } else {
            existingList.add(VpnServerInfo(name = serverName, key = key))
        }
        saveVpnKeys(existingList)
    }


    fun deleteVpnKey(serverName: String) {
        val existingList = getVpnKeys().toMutableList()
        val index = existingList.indexOfFirst { it.name == serverName }
        if (index >= 0) {
            existingList.removeAt(index)
            saveVpnKeys(existingList)
        }
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

    fun saveSelectedApps(apps: List<String>) {
        preferences.edit().putStringSet(KEY_SELECTED_APPS, apps.toSet()).apply()
    }

    fun getSelectedApps(): Set<String>? {
        return preferences.getStringSet(KEY_SELECTED_APPS, null)
    }

    companion object {
        private const val PREFS_NAME = "outline_vpn_prefs"
        private const val KEY_VPN_LIST = "vpn_keys_list"
        private const val KEY_VPN_START_TIME = "vpn_start_time"
        private const val KEY_SERVER_NAME = "server_name"
        private const val KEY_SELECTED_DNS = "selected_dns"
        private const val KEY_SELECTED_APPS = "selected_apps"
        private const val KEY_SELECTED_THEME = "selected_theme"
    }
}
