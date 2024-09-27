package app.android.outlinevpntv.data.remote

import android.util.Patterns
import app.android.outlinevpntv.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ServerIconProvider {
    suspend fun icon(serverIp: String): String?

    class FlagsApiDotCom(
        private val ipCountryCodeProvider: IpCountryCodeProvider,
        private val preferencesManager: PreferencesManager
    ) : ServerIconProvider {

        override suspend fun icon(serverIp: String): String? = withContext(Dispatchers.IO) {
            if (serverIp == "127.0.0.1" || serverIp == "0.0.0.0" || !Patterns.IP_ADDRESS.matcher(serverIp).matches()) {
                return@withContext null
            }

            val savedFlagUrl = preferencesManager.getFlagUrl(serverIp)
            if (savedFlagUrl != null) {
                return@withContext savedFlagUrl
            }

            val countryCode = ipCountryCodeProvider.countryCode(serverIp) ?: return@withContext null

            val serverIconUrl = API_URL.format(countryCode)
            preferencesManager.saveFlagUrl(serverIp, serverIconUrl)

            return@withContext serverIconUrl
        }

        companion object {
            private const val API_URL = "https://flagsapi.com/%s/flat/64.png"
        }
    }
}