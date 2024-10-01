package app.android.outlinevpntv.data.remote

import android.net.Uri
import android.util.Base64
import app.android.outlinevpntv.data.model.ShadowSocksInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
            return@withContext if (ssUrl.startsWith("ssconf://")) {
                parseShadowSocksConfUrl(ssUrl)
            } else if (ssUrl.startsWith("ss://")) {
                parseShadowSocksSsUrl(ssUrl)
            } else {
                throw IllegalArgumentException("Invalid URL format")
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

            val urlWithoutFragment = ssConfUrl.split("#")[0]
            val httpsUrl = urlWithoutFragment.replace("ssconf://", "https://")

            val jsonResponse = jsonFetch.fetch(httpsUrl)
            val jsonObject = JSONObject(jsonResponse)
            val host = jsonObject.getString("server")
            val portString = jsonObject.getString("server_port")
            val port = portString.toInt()
            val password = jsonObject.getString("password")
            val method = jsonObject.getString("method")
            val prefix = if (!jsonObject.isNull("prefix")) jsonObject.getString("prefix") else null

            return ShadowSocksInfo(method, password, host, port, prefix)
        }

        private fun parseShadowSocksSsUrl(ssUrl: String): ShadowSocksInfo {
            val matchResult = SS_URL_REGEX.find(ssUrl)
            val groups =
                matchResult?.groupValues ?: throw IllegalArgumentException("Invalid link format")

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

            val host = groups[2]
            val port = groups[3].toInt()

            val queryParams = groups[4].split("&").associate { q ->
                q.split("=").let {
                    it[0] to Uri.decode(it.getOrElse(1) { "" })
                }
            }
            val serverName = groups[5]

            return ShadowSocksInfo(method, password, host, port, queryParams["prefix"])
        }
    }

    companion object {
        private val SS_URL_REGEX = Regex("ss://([^@]+)@([^:]+):(\\d+)(?:/?\\?([^#]+))?(?:#(.+))?")
    }
}