package app.android.outlinevpntv.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AppSelectionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val appList = remember { mutableStateListOf<AppInfo>() }

    LaunchedEffect(Unit) {
        val apps = getInstalledApps(requireContext = { context })
        appList.clear()
        appList.addAll(apps)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Выберите приложение", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(appList) { appInfo ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            AppListItem(appInfo, onAppSelected = { selectedApp, isSelected ->
                                val index = appList.indexOf(selectedApp)
                                if (index >= 0) {
                                    appList[index] = selectedApp.copy(isSelected = isSelected)
                                    updateSelectedApps(context, selectedApp.packageName, isSelected)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppListItem(appInfo: AppInfo, onAppSelected: (AppInfo, Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAppSelected(appInfo, !appInfo.isSelected)
            }
            .padding(8.dp)
    ) {
        val appIconBitmap = appInfo.icon.toBitmap()
        appIconBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = appInfo.appName, modifier = Modifier.weight(1f))
        Checkbox(
            checked = appInfo.isSelected,
            onCheckedChange = {
                onAppSelected(appInfo, it)
            }
        )
    }
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    var isSelected: Boolean = false
)


fun getInstalledApps(requireContext: () -> Context): List<AppInfo> {

    val pm = requireContext().packageManager
    val installedApps = pm.getInstalledApplications(0)
    val selectedApps = PreferenceManager.getDefaultSharedPreferences(requireContext())
        .getStringSet("selected_apps", setOf()) ?: setOf()

    return installedApps
        .filter { it.packageName != requireContext().packageName }
        .map {
            AppInfo(
                pm.getApplicationLabel(it).toString(),
                it.packageName,
                pm.getApplicationIcon(it.packageName),
                selectedApps.contains(it.packageName)
            )
        }
        .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
}




fun updateSelectedApps(context: android.content.Context, packageName: String, isSelected: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val selectedApps = prefs.getStringSet("selected_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

    if (isSelected) {
        selectedApps.add(packageName)
    } else {
        selectedApps.remove(packageName)
    }

    prefs.edit().putStringSet("selected_apps", selectedApps).apply()
}


fun Drawable.toBitmap(): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        when (this) {
            is BitmapDrawable -> this.bitmap
            is AdaptiveIconDrawable -> {
                val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                this.setBounds(0, 0, canvas.width, canvas.height)
                this.draw(canvas)
                bitmap
            }
            else -> null
        }
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}
