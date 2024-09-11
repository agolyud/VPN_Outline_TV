package app.android.outlinevpntv.data.remote

import android.os.Build
import android.util.Log
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
            connection.setRequestProperty("User-Agent", "Android/${Build.VERSION.RELEASE} (${Build.MODEL}; ${Build.MANUFACTURER})")
            connection.connect()

            if (connection.responseCode == 429) {
                Log.e("IP_REQUEST", "Too many requests: ${connection.responseCode}")
                throw Exception("Too many requests: ${connection.responseCode}")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch country code: ${connection.responseCode}")
            }

            val jsonResponse = connection.inputStream.bufferedReader().readText()
            val jsonObject = JSONObject(jsonResponse)
            val countryCode = jsonObject.optString("country_code", "Unknown")
            countryCode
        } catch (e: Exception) {
            // Логируем ошибку
            Log.e("IP_REQUEST", "Error fetching country code: ${e.message}")
            "Unknown"
        }
    }
}

