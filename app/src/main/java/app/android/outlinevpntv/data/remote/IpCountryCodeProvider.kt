package app.android.outlinevpntv.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

interface IpCountryCodeProvider {
    suspend fun countryCode(serverIp: String): String?

    class IpApiDotCo(private val fetch: RemoteJSONFetch) : IpCountryCodeProvider {

        override suspend fun countryCode(serverIp: String): String? = withContext(Dispatchers.IO) {
            val urlString = API_URL.format(serverIp)
            val response = runCatching { fetch.fetch(urlString) }
            if (response.isFailure) {
                return@withContext null
            }
            val jsonObject = JSONObject(response.getOrThrow())
            val countryCode = if (jsonObject.has("country_code"))
                jsonObject.getString("country_code")
            else null
            return@withContext countryCode
        }

        companion object {
            private const val API_URL = "https://ipapi.co/%s/json/"
        }
    }
}