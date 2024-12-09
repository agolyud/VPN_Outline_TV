package app.android.outlinevpntv.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.android.outlinevpntv.R
import app.android.outlinevpntv.data.preferences.PreferencesManager
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    preferencesManager: PreferencesManager,
    onDnsSelected: (String) -> Unit
) {
    var selectedDns by remember {
        mutableStateOf(preferencesManager.getSelectedDns() ?: "8.8.8.8")
    }


    val selectedApps = remember { mutableStateListOf<String>() }
    var isAppSelectionDialogOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val packageManager = context.packageManager

    LaunchedEffect(Unit) {
        if (preferencesManager.getSelectedDns().isNullOrEmpty()) {
            preferencesManager.saveSelectedDns("8.8.8.8")
            onDnsSelected("8.8.8.8")
        } else {
            selectedDns = preferencesManager.getSelectedDns() ?: "8.8.8.8"
        }

        val savedApps = preferencesManager.getSelectedApps()
        if (savedApps.isNullOrEmpty()) {
            selectedApps.clear()
            selectedApps.add("all_apps")
            preferencesManager.saveSelectedApps(selectedApps.toList())
        } else {
            selectedApps.clear()
            selectedApps.addAll(savedApps)
        }
    }

    if (isAppSelectionDialogOpen) {
        AppSelectionDialog(
            onDismiss = { isAppSelectionDialogOpen = false },
            initialSelectedApps = selectedApps.toList(),
            onAppsSelected = { apps ->
                selectedApps.remove("all_apps")
                selectedApps.clear()
                selectedApps.addAll(apps)

                preferencesManager.saveSelectedApps(selectedApps.toList())
            }
        )
    }


    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsDialogSectionTitle(text = "Server DNS")
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = "Google DNS",
                        selected = selectedDns == "8.8.8.8",
                        onClick = {
                            preferencesManager.saveSelectedDns("8.8.8.8")
                            selectedDns = "8.8.8.8"
                            onDnsSelected("8.8.8.8")
                        }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Cloudflare DNS",
                        selected = selectedDns == "1.1.1.1",
                        onClick = {
                            preferencesManager.saveSelectedDns("1.1.1.1")
                            selectedDns = "1.1.1.1"
                            onDnsSelected("1.1.1.1")
                        }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Yandex DNS",
                        selected = selectedDns == "77.88.8.8",
                        onClick = {
                            preferencesManager.saveSelectedDns("77.88.8.8")
                            selectedDns = "77.88.8.8"
                            onDnsSelected("77.88.8.8")
                        }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "AdGuard DNS",
                        selected = selectedDns == "94.140.14.14",
                        onClick = {
                            preferencesManager.saveSelectedDns("94.140.14.14")
                            selectedDns = "94.140.14.14"
                            onDnsSelected("94.140.14.14")
                        }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "OpenDNS",
                        selected = selectedDns == "208.67.222.222",
                        onClick = {
                            preferencesManager.saveSelectedDns("208.67.222.222")
                            selectedDns = "208.67.222.222"
                            onDnsSelected("208.67.222.222")
                        }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Quad9 DNS",
                        selected = selectedDns == "9.9.9.9",
                        onClick = {
                            preferencesManager.saveSelectedDns("9.9.9.9")
                            selectedDns = "9.9.9.9"
                            onDnsSelected("9.9.9.9")
                        }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Comodo Secure DNS",
                        selected = selectedDns == "8.26.56.26",
                        onClick = {
                            preferencesManager.saveSelectedDns("8.26.56.26")
                            selectedDns = "8.26.56.26"
                            onDnsSelected("8.26.56.26")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsDialogSectionTitle(
                    text = stringResource(id = R.string.services)
                )
                Text(
                    text = stringResource(id = R.string.services_description),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                )
                Column {

                    Button(
                        onClick = { isAppSelectionDialogOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text( stringResource(id = R.string.add_an_application))
                    }


                    SettingsDialogThemeChooserRow(
                        text = stringResource(id = R.string.all_applications),
                        selected = selectedApps.contains("all_apps"),
                        onClick = {
                            if (selectedApps.contains("all_apps")) {
                                selectedApps.remove("all_apps")
                            } else {
                                selectedApps.clear()
                                selectedApps.add("all_apps")
                            }
                        }
                    )

                    selectedApps.filter { it != "all_apps" }.forEach { packageName ->
                        val appName = try {
                            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                            packageManager.getApplicationLabel(applicationInfo).toString()
                        } catch (e: Exception) {
                            packageName
                        }
                        SettingsDialogThemeChooserRow(
                            text = appName,
                            selected = true,
                            onClick = {
                                selectedApps.remove(packageName)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                LinksPanel()
            }
        },
        confirmButton = {
            Text(
                text = stringResource(id = R.string.save),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        preferencesManager.saveSelectedApps(selectedApps.toList())
                        onDismiss()
                    },
            )
        }
    )
}





@Composable
fun LinksPanel() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.getString(R.string.github_link))
                            )
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {}
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
                    contentDescription = "Open GitHub",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = context.getString(R.string.by_author),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                )
            }

            NiaTextButton(
                onClick = { uriHandler.openUri(context.getString(R.string.LICENSE)) },
            ) {
                Text(
                    text = stringResource(id = R.string.license),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}




@Composable
fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun NiaTextButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        content()
    }
}

@Preview
@Composable
private fun PreviewCustomSettingsDialog() {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)

    SettingsDialog(
        onDismiss = {},
        preferencesManager = preferencesManager,
        onDnsSelected = { dns ->}
    )
}
