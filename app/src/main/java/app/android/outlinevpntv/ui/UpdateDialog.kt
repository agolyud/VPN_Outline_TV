package app.android.outlinevpntv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.android.outlinevpntv.R

@Composable
fun UpdateDialog(
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    downloadProgress: Int,
    currentVersion: String,
    latestVersion: String?,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.app_update_available),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                if (downloadProgress > 0 && downloadProgress < 100) {
                    LinearProgressIndicator(progress = downloadProgress / 100f)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.loading) + " $downloadProgress%")
                } else {
                    if (latestVersion != null) {
                        Text(
                            stringResource(id = R.string.your_version) + ": $currentVersion\n" + stringResource(id = R.string.upgrade_to) +": $latestVersion",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    } else {
                        Text(stringResource(id = R.string.failed_to_get_latest_version) )
                    }
                }
            }
        },
        confirmButton = {
            if (downloadProgress == 0 && latestVersion != null) {
                TextButton(onClick = onUpdate) {
                    Text(stringResource(id = R.string.update))
                }
            }
        },
        dismissButton = {
            if (downloadProgress == 0) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun PreviewUpdateDialog() {
    UpdateDialog(
        onUpdate = {},
        onDismiss = {},
        downloadProgress = 50,
        currentVersion = "1.0.0",
        latestVersion = "2.0.0"
    )
}
