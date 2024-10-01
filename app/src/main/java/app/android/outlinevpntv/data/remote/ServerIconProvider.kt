package app.android.outlinevpntv.data.remote

import android.util.Patterns
import app.android.outlinevpntv.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

interface ServerIconProvider {
    suspend fun icon(serverHost: String): String?

    class FlagsApiDotCom(
        private val ipCountryCodeProvider: IpCountryCodeProvider,
        private val preferencesManager: PreferencesManager
    ) : ServerIconProvider {

        override suspend fun icon(serverHost: String): String? = withContext(Dispatchers.IO) {

            val savedFlagUrl = preferencesManager.getFlagUrl(serverHost)
            if (savedFlagUrl != null) {
                return@withContext savedFlagUrl
            }

            val serverIp = if (Patterns.DOMAIN_NAME.matcher(serverHost).matches()) {
                try {
                    InetAddress.getByName(serverHost).hostAddress
                } catch (_: Exception) {
                    return@withContext null
                }
            } else serverHost

            if (serverIp == "127.0.0.1" || serverIp == "0.0.0.0" || !Patterns.IP_ADDRESS.matcher(serverIp).matches()) {
                return@withContext null
            }

            val countryCode = ipCountryCodeProvider.countryCode(serverIp) ?: return@withContext null

            val serverIconUrl = API_URL.format(countryCode)
            preferencesManager.saveFlagUrl(serverHost, serverIconUrl)

            return@withContext serverIconUrl
        }

        companion object {
            private const val API_URL = "https://flagsapi.com/%s/flat/64.png"
        }
    }
}