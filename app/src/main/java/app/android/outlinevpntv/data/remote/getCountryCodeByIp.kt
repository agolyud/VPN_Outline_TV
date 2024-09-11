package app.android.outlinevpntv.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun getCountryCodeByIp(ip: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipapi.co/$ip/json/")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch country code: ${connection.responseCode}")
            }

            val jsonResponse = connection.inputStream.bufferedReader().readText()
            val jsonObject = JSONObject(jsonResponse)
            jsonObject.getString("country_code")
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
