package app.android.outlinevpntv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.android.outlinevpntv.R

@Composable
fun UpdateDialog(
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    isDownloading: Boolean,
    downloadProgress: Int,
    currentVersion: String,
    latestVersion: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (!isDownloading) R.string.app_update_available
                        else R.string.app_update_downloading
                ),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        drawStopIndicator = {}
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.loading_progress, downloadProgress))
                } else {
                    Row {
                        Text(
                            stringResource(id = R.string.your_version, currentVersion),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text(
                            stringResource(id = R.string.upgrade_to, latestVersion),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                TextButton(onClick = onUpdate) {
                    Text(stringResource(id = R.string.update))
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDownloading,
            dismissOnClickOutside = !isDownloading
        )
    )
}



@Preview(showBackground = true)
@Composable
fun PreviewUpdateDialog() {
    UpdateDialog(
        onUpdate = {},
        onDismiss = {},
        isDownloading = false,
        downloadProgress = 50,
        currentVersion = "1.0.0",
        latestVersion = "2.0.0"
    )
}