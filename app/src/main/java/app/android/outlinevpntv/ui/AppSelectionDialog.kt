package app.android.outlinevpntv.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import app.android.outlinevpntv.R
import app.android.outlinevpntv.data.model.AppInfo
import app.android.outlinevpntv.domain.installedapps.InstalledApps


@Composable
fun AppSelectionDialog(
    onDismiss: () -> Unit,
    initialSelectedApps: List<String>,
    onAppsSelected: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val appList = remember { mutableStateListOf<AppInfo>() }
    val selectedApps = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        selectedApps.clear()
        selectedApps.addAll(initialSelectedApps.filter { it != "all_apps" })

        val apps = InstalledApps(requireContext = { context }, selectedApps = selectedApps)
        appList.clear()
        appList.addAll(apps)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.select_applications),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(appList) { appInfo ->
                        AppListItem(appInfo, onAppSelected = { selectedApp, isSelected ->
                            val index = appList.indexOf(selectedApp)
                            if (index >= 0) {
                                appList[index] = selectedApp.copy(isSelected = isSelected)
                                if (isSelected) {
                                    selectedApps.add(selectedApp.packageName)
                                } else {
                                    selectedApps.remove(selectedApp.packageName)
                                }
                            }
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text( stringResource(id = R.string.cancel))

                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onAppsSelected(selectedApps.toList())
                        onDismiss()
                    }) {
                        Text( stringResource(id = R.string.save))

                    }
                }
            }
        }
    }
}



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

