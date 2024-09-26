package app.android.outlinevpntv.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


object GitHubUpdateChecker {
    private const val REPO_OWNER = "agolyud"
    private const val REPO_NAME = "VPN_Outline_TV"
    private const val API_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"

    private val client = OkHttpClient()


    suspend fun getLatestReleaseVersion(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(API_URL)
                .get()
                .build()


            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData ?: return@withContext null)
                jsonObject.getString("tag_name")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


suspend fun checkForUpdate(currentVersion: String): Boolean {
    val latestVersion = GitHubUpdateChecker.getLatestReleaseVersion()
    return if (latestVersion != null) {
        latestVersion > currentVersion
    } else {
        false
    }
}
