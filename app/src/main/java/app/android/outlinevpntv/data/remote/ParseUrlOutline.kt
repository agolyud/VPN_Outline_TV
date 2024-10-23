package app.android.outlinevpntv.data.remote

import android.net.Uri
import android.util.Base64
import android.util.Log
import app.android.outlinevpntv.data.model.ShadowSocksInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

interface ParseUrlOutline {
    suspend fun parse(ssUrl: String): ShadowSocksInfo
    fun extractServerHost(ssUrl: String): String?

    interface Validate {
        fun validate(ssUrl: String): Boolean

        class Base : Validate {
            override fun validate(ssUrl: String): Boolean {
                return ssUrl.startsWith("ssconf://") || SS_URL_REGEX.matches(ssUrl)
            }
        }
    }

    class Base(private val jsonFetch: RemoteJSONFetch) : ParseUrlOutline {

        override suspend fun parse(ssUrl: String): ShadowSocksInfo = withContext(Dispatchers.IO) {
            Log.d("ParseUrl", "Parsing URL: $ssUrl")
            return@withContext when {
                ssUrl.startsWith("ssconf://") -> parseShadowSocksConfUrl(ssUrl)
                ssUrl.startsWith("ss://") -> parseShadowSocksSsUrl(ssUrl)
                else -> {
                    Log.e("ParseUrl", "Invalid URL format: $ssUrl")
                    throw IllegalArgumentException("Invalid URL format")
                }
            }
        }

        override fun extractServerHost(ssUrl: String): String? {
            val parsedUrl = Uri.parse(ssUrl)
            return parsedUrl.host
        }

        private suspend fun parseShadowSocksConfUrl(ssConfUrl: String): ShadowSocksInfo {
            if (!ssConfUrl.startsWith("ssconf://")) {
                throw IllegalArgumentException("Invalid ssconf URL format")
            }

            Log.d("ParseUrl", "Parsing ssconf URL: $ssConfUrl")

            // Извлекаем URL без фрагмента
            val urlWithoutFragment = ssConfUrl.split("#")[0]
            val httpsUrl = urlWithoutFragment.replace("ssconf://", "https://")
            // Логируем модифицированный URL
            Log.d("ParseUrl", "Fetching JSON from URL: $httpsUrl")

            val jsonResponse = fetchJsonFromUrl(httpsUrl)
            Log.d("ParseUrl", "JSON Response: $jsonResponse")

            // Проверяем, является ли ответ строкой с ss:// URL
            if (jsonResponse.startsWith("ss://")) {
                Log.d("ParseUrl", "Embedded ss URL found in response: $jsonResponse")
                // Парсим как обычный ss URL
                return parseShadowSocksSsUrl(jsonResponse)
            }

            // Если это JSON, продолжаем его разбор
            val jsonObject = JSONObject(jsonResponse)

            val host = jsonObject.getString("server")
            val port = jsonObject.getInt("server_port")
            val password = jsonObject.getString("password")
            val method = jsonObject.getString("method")
            val prefix = jsonObject.optString("prefix", null)

            // Логируем извлеченные данные
            Log.d("ParseUrl", "Parsed JSON - Host: $host, Port: $port, Method: $method, Password: $password, Prefix: $prefix")

            return ShadowSocksInfo(method, password, host, port, prefix)
        }


        fun fetchJsonFromUrl(urlString: String): String {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            return connection.inputStream.bufferedReader().use { it.readText() }
        }


        private fun parseShadowSocksSsUrl(ssUrl: String): ShadowSocksInfo {
            Log.d("ParseUrl", "Parsing ss URL: $ssUrl")

            val matchResult = SS_URL_REGEX.find(ssUrl)
            val groups = matchResult?.groupValues ?: throw IllegalArgumentException("Invalid link format")

            Log.d("ParseUrl", "Match groups: $groups")

            val base64EncodedMethodAndPasswordString = groups[1]
            val methodAndPasswordString = String(
                Base64.decode(base64EncodedMethodAndPasswordString, Base64.DEFAULT),
                StandardCharsets.UTF_8
            )
            val methodAndPassword = methodAndPasswordString.split(":")
            if (methodAndPassword.size != 2) {
                throw IllegalArgumentException("Invalid decoded info format")
            }
            val (method, password) = methodAndPassword

            Log.d("ParseUrl", "Decoded Method: $method, Password: $password")

            val host = groups[2]
            val port = groups[3].toInt()

            Log.d("ParseUrl", "Host: $host, Port: $port")

            val queryParams = groups[4].split("&").associate { q ->
                q.split("=").let {
                    it[0] to Uri.decode(it.getOrElse(1) { "" })
                }
            }
            val serverName = groups[5]

            Log.d("ParseUrl", "Query params: $queryParams, Server name: $serverName")

            return ShadowSocksInfo(method, password, host, port, queryParams["prefix"])
        }


    }

        companion object {
        private val SS_URL_REGEX = Regex("ss://([^@]+)@([^:]+):(\\d+)(?:/?\\?([^#]+))?(?:#(.+))?")
    }
}