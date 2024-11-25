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
                return when {
                    ssUrl.startsWith("ssconf://") -> {
                        ssUrl.contains(".json") && ssUrl.contains("outline")
                    }
                    ssUrl.startsWith("ss://") -> {
                        val ssData = ssUrl.substringAfter("ss://")
                        try {
                            if (ssData.contains("@")) {
                                validatePartialEncoded(ssData)
                            } else {

                                isValidBase64(ssData)
                            }
                        } catch (e: Exception) {
                            false
                        }
                    }
                    else -> false
                }
            }

            private fun validatePartialEncoded(ssData: String): Boolean {
                val matchResult = SS_URL_REGEX.find("ss://$ssData") ?: return false
                val groups = matchResult.groupValues
                if (groups.size < 4) return false

                val base64Encoded = groups[1]
                return isValidBase64(base64Encoded)
            }

            private fun isValidBase64(s: String): Boolean {
                return try {
                    val decoded = Base64.decode(s, Base64.DEFAULT)
                    decoded.isNotEmpty()
                } catch (e: IllegalArgumentException) {
                    false
                }
            }

            companion object {
                private val SS_URL_REGEX = Regex(
                    "ss://([^@]+)@([^:]+):(\\d+)(?:/\\?([^#]+))?(?:#(.+))?"
                )
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

            val urlWithoutFragment = ssConfUrl.split("#")[0]
            val httpsUrl = urlWithoutFragment.replace("ssconf://", "https://")
            Log.d("ParseUrl", "Fetching JSON from URL: $httpsUrl")

            val jsonResponse = fetchJsonFromUrl(httpsUrl)
            Log.d("ParseUrl", "JSON Response: $jsonResponse")

            if (jsonResponse.startsWith("ss://")) {
                Log.d("ParseUrl", "Embedded ss URL found in response: $jsonResponse")
                return parseShadowSocksSsUrl(jsonResponse)
            }

            val jsonObject = JSONObject(jsonResponse)

            val host = jsonObject.getString("server")
            val port = jsonObject.getInt("server_port")
            val password = jsonObject.getString("password")
            val method = jsonObject.getString("method")
            val prefix = jsonObject.optString("prefix", null)

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

            val ssData = ssUrl.substringAfter("ss://")

            return if (ssData.contains("@")) {
                Log.d("ParseUrl", "Detected partial encoding format")
                parsePartialEncodedSsUrl(ssData)
            } else {
                Log.d("ParseUrl", "Detected full encoding format")
                parseFullyEncodedSsUrl(ssData)
            }
        }

        private fun parsePartialEncodedSsUrl(ssData: String): ShadowSocksInfo {
            val matchResult = SS_URL_REGEX.find("ss://$ssData")
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

            val queryParams = groups.getOrNull(4)?.split("&")?.associate { q ->
                q.split("=").let {
                    it[0] to Uri.decode(it.getOrElse(1) { "" })
                }
            } ?: emptyMap()
            val serverName = groups.getOrNull(5)

            Log.d("ParseUrl", "Query params: $queryParams, Server name: $serverName")

            return ShadowSocksInfo(method, password, host, port, queryParams["prefix"])
        }

        private fun parseFullyEncodedSsUrl(ssData: String): ShadowSocksInfo {
            val decodedData = String(
                Base64.decode(ssData, Base64.DEFAULT),
                StandardCharsets.UTF_8
            )

            Log.d("ParseUrl", "Decoded data: $decodedData")

            val methodPasswordAndServer = decodedData.split("@")
            if (methodPasswordAndServer.size != 2) {
                throw IllegalArgumentException("Invalid decoded data format")
            }

            val methodAndPassword = methodPasswordAndServer[0]
            val hostAndPort = methodPasswordAndServer[1]

            val methodPasswordSplit = methodAndPassword.split(":", limit = 2)
            if (methodPasswordSplit.size != 2) {
                throw IllegalArgumentException("Invalid method and password format")
            }
            val (method, password) = methodPasswordSplit

            val hostPortSplit = hostAndPort.split(":", limit = 2)
            if (hostPortSplit.size != 2) {
                throw IllegalArgumentException("Invalid host and port format")
            }
            val host = hostPortSplit[0]
            val port = hostPortSplit[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid port number")

            Log.d("ParseUrl", "Method: $method, Password: $password, Host: $host, Port: $port")

            return ShadowSocksInfo(method, password, host, port, null)
        }

        companion object {
            private val SS_URL_REGEX = Regex("ss://([^@]+)@([^:]+):(\\d+)(?:/\\?([^#]+))?(?:#(.+))?")
        }
    }
}

