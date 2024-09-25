package app.android.outlinevpntv.data.remote

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

interface RemoteJSONFetch {
    suspend fun fetch(urlString: String): String

    class HttpURLConnectionJSONFetch : RemoteJSONFetch {

        override suspend fun fetch(urlString: String): String = withContext(Dispatchers.IO) {
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "User-Agent",
                "Android/${Build.VERSION.RELEASE} (${Build.MODEL}; ${Build.MANUFACTURER})"
            )
            connection.connect()

            if (connection.responseCode == 429) {
                Log.e("HttpURLConnectionFetch", "Too many requests: ${connection.responseCode}")
                throw Exception("Too many requests: ${connection.responseCode}")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch: ${connection.responseCode}")
            }

            if (connection.contentType != "application/json") {
                throw Exception("Invalid content type: ${connection.responseCode} ${connection.contentType}")
            }

            return@withContext connection.inputStream.bufferedReader().use { it.readText() }
        }
    }
}