package app.android.outlinevpntv.domain

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import app.android.outlinevpntv.domain.GitHubUpdateChecker.REPO_NAME
import app.android.outlinevpntv.domain.GitHubUpdateChecker.REPO_OWNER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File


object GitHubUpdateChecker {
    const val REPO_OWNER = "agolyud"
    const val REPO_NAME = "VPN_Outline_TV"
    const val API_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"

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


suspend fun downloadAndInstallApk(context: Context, onProgress: (Int) -> Unit) {
    val url = "https://github.com/$REPO_OWNER/$REPO_NAME/releases/latest/download/OutlineVPNtv.apk"
    val apkFile = File(context.cacheDir, "OutlineVPNtv.apk")

    try {
        if (!apkFile.exists()) {
            downloadApk(url, apkFile, onProgress)
        }
        withContext(Dispatchers.Main) {
            installApk(context, apkFile)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Ошибка при обновлении", Toast.LENGTH_LONG).show()
        }
    }
}

private suspend fun downloadApk(url: String, outputFile: File, onProgress: (Int) -> Unit) {
    withContext(Dispatchers.IO) {
        val request = okhttp3.Request.Builder().url(url).build()
        val response = OkHttpClient().newCall(request).execute()
        val inputStream = response.body?.byteStream()
        val totalBytes = response.body?.contentLength() ?: -1
        var downloadedBytes = 0L

        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    // Рассчитываем процент прогресса
                    val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                    onProgress(progress)
                }
            }
        }
    }
}

private fun installApk(context: Context, apkFile: File) {
    val apkUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        apkFile
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}



