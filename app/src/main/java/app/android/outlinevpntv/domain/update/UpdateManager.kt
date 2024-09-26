package app.android.outlinevpntv.domain.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

interface UpdateManager {
    suspend fun checkForAppUpdates(currentVersion: String): UpdateStatus
    suspend fun downloadAndInstallLatestApk(
        onProgress: (Int) -> Unit,
        onError: (Throwable) -> Unit
    )

    sealed class UpdateStatus {
        class Available(val latestVersion: String) : UpdateStatus()
        data object Unavailable : UpdateStatus()
    }

    class Github(
        private val context: Context,
        private val repoOwner: String = REPO_OWNER,
        private val repoName: String = REPO_NAME,
        private val apkFileName: String = APK_FILE_NAME,
        private val client: OkHttpClient = OkHttpClient(),
    ) : UpdateManager {

        override suspend fun checkForAppUpdates(
            currentVersion: String
        ): UpdateStatus = withContext(Dispatchers.IO) {
            val latestVersion = getLatestReleaseVersion()
            if (latestVersion != null && latestVersion > currentVersion) {
                return@withContext UpdateStatus.Available(latestVersion)
            }
            return@withContext UpdateStatus.Unavailable
        }

        override suspend fun downloadAndInstallLatestApk(
            onProgress: (Int) -> Unit,
            onError: (Throwable) -> Unit
        ) {
            val url = APK_FILE_ENDPOINT.format(repoOwner, repoName, apkFileName)
            val apkFile = File(context.cacheDir, apkFileName)

            try {
                downloadApk(url, apkFile, onProgress)
                withContext(Dispatchers.Main) {
                    installApk(context, apkFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e)
            }
        }

        private suspend fun getLatestReleaseVersion(): String? = withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(LATEST_VERSION_ENDPOINT.format(repoOwner, repoName))
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw RuntimeException(response.message)
                    }
                    val responseData = response.body!!.string()
                    val jsonObject = JSONObject(responseData)
                    return@withContext if (jsonObject.has("tag_name"))
                        jsonObject.getString("tag_name")
                    else
                        null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

        private suspend fun downloadApk(
            url: String,
            outputFile: File,
            onProgress: (Int) -> Unit
        ) = withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException(response.message)
                }

                val body = response.body!!

                val totalBytes = body.contentLength()
                if (outputFile.exists()) {
                    if (outputFile.length() != totalBytes) {
                        outputFile.delete()
                    } else {
                        // Already downloaded
                        return@withContext
                    }
                }

                var downloadedBytes = 0L
                body.byteStream().use { input ->
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

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

        companion object {
            private const val REPO_OWNER = "agolyud"
            private const val REPO_NAME = "VPN_Outline_TV"
            private const val LATEST_VERSION_ENDPOINT =
                "https://api.github.com/repos/%s/%s/releases/latest"
            private const val APK_FILE_NAME = "OutlineVPNtv.apk"
            private const val APK_FILE_ENDPOINT =
                "https://github.com/%s/%s/releases/latest/download/%s"
        }
    }
}