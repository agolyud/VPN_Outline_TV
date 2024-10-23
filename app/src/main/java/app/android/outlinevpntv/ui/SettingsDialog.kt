package app.android.outlinevpntv.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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


@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // Выбор DNS
                SettingsDialogSectionTitle(text = "Выбор DNS")
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = "Google DNS",
                        selected = true,
                        onClick = { /* TODO: Обработчик для выбора Google DNS */ }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Cloudflare DNS",
                        selected = false,
                        onClick = { /* TODO: Обработчик для выбора Cloudflare DNS */ }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Yandex DNS",
                        selected = false,
                        onClick = { /* TODO: Обработчик для выбора Yandex DNS */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Блок с Службами
                SettingsDialogSectionTitle(text = "Службы")
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = "Служба 1",
                        selected = false,
                        onClick = { /* TODO: Обработчик для выбора Службы 1 */ }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Служба 2",
                        selected = true,
                        onClick = { /* TODO: Обработчик для выбора Службы 2 */ }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Служба 3",
                        selected = false,
                        onClick = { /* TODO: Обработчик для выбора Службы 3 */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinksPanel()

            }
        },
        confirmButton = {
            Text(
                text = "Закрыть",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onDismiss() },
            )
        }
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinksPanel() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

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
                text = "Лицензия",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
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
    SettingsDialog(onDismiss = {})
}