package app.android.outlinevpntv.utils

import android.content.Context

fun versionName(context: Context): String {
    return try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}