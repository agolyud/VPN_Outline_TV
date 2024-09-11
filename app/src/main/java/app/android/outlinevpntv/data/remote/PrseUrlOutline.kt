package app.android.outlinevpntv.data.remote

import android.util.Base64
import app.android.outlinevpntv.data.model.ShadowsocksInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

suspend fun parseShadowsocksUrl(ssUrl: String): ShadowsocksInfo {
    return if (ssUrl.startsWith("ssconf://")) {
        parseShadowsocksConfUrl(ssUrl)
    } else if (ssUrl.startsWith("ss://")) {
        parseShadowsocksSsUrl(ssUrl)
    } else {
        throw IllegalArgumentException("Invalid URL format")
    }
}

private suspend fun parseShadowsocksConfUrl(ssConfUrl: String): ShadowsocksInfo = withContext(
    Dispatchers.IO) {

    if (!ssConfUrl.startsWith("ssconf://")) {
        throw IllegalArgumentException("Invalid ssconf URL format")
    }

    val urlWithoutFragment = ssConfUrl.split("#")[0]
    val httpsUrl = urlWithoutFragment.replace("ssconf://", "https://")
    val jsonResponse = fetchJsonFromUrl(httpsUrl)
    val jsonObject = JSONObject(jsonResponse)
    val host = jsonObject.getString("server")
    val portString = jsonObject.getString("server_port")
    val port = portString.toInt()
    val password = jsonObject.getString("password")
    val method = jsonObject.getString("method")

    ShadowsocksInfo(method, password, host, port)
}

fun fetchJsonFromUrl(urlString: String): String {
    val url = URL(urlString)
    val connection = url.openConnection() as HttpURLConnection
    return connection.inputStream.bufferedReader().use { it.readText() }
}

fun parseShadowsocksSsUrl(ssUrl: String): ShadowsocksInfo {
    val regex = Regex("ss://([^@]+)@([^:]+):(\\d+)(?:\\?.*|#.*)?")
    val matchResult = regex.find(ssUrl)
    val groups = matchResult?.groupValues ?: throw IllegalArgumentException("Invalid link format")

    val decodedInfo = String(Base64.decode(groups[1], Base64.DEFAULT), StandardCharsets.UTF_8)
    val parts = decodedInfo.split(":")
    if (parts.size != 2) throw IllegalArgumentException("Invalid decoded info format")

    return ShadowsocksInfo(parts[0], parts[1], groups[2], groups[3].toInt())
}