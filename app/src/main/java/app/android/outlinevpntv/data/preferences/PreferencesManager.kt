package app.android.outlinevpntv.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class VpnServerInfo(
    val name: String,
    val key: String
)

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

    /** Добавляем или обновляем VPN key. Если key с таким именем уже есть, обновим его. */
    fun addOrUpdateVpnKey(serverName: String, key: String) {
        val existingList = getVpnKeys().toMutableList()
        val index = existingList.indexOfFirst { it.name == serverName }
        if (index >= 0) {
            // Обновляем существующий
            existingList[index] = VpnServerInfo(name = serverName, key = key)
        } else {
            // Добавляем новый
            existingList.add(VpnServerInfo(name = serverName, key = key))
        }
        saveVpnKeys(existingList)
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
    }
}
