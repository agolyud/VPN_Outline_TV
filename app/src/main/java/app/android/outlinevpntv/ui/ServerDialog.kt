package app.android.outlinevpntv.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.android.outlinevpntv.R
import app.android.outlinevpntv.data.model.ShadowsocksInfo
import app.android.outlinevpntv.data.remote.parseShadowsocksUrl
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp


@Composable
fun ServerDialog(
    currentName: String,
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, ShadowsocksInfo?) -> Unit,
    onClear: () -> Unit
) {
    var serverName by remember { mutableStateOf(currentName) }
    var serverKey by remember { mutableStateOf(currentKey) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.edit_server_info),
                    fontSize = 17.sp // Уменьшаем размер текста
                )
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    val clipboardText = clipboardManager.getText()?.text
                    if (!clipboardText.isNullOrEmpty()) {
                        serverKey = clipboardText
                    } else {
                        Toast.makeText(context, R.string.clipboard_empty, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(imageVector = Icons.Filled.ContentPaste, contentDescription = "Paste from clipboard")
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text(stringResource(id = R.string.server_name)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = serverKey,
                    onValueChange = { serverKey = it },
                    label = { Text(stringResource(id = R.string.outline_key)) },
                    singleLine = true
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    serverName = "Server Name"
                    serverKey = ""
                    onClear()
                }) {
                    Text(stringResource(id = R.string.clear))
                }

                TextButton(onClick = {
                    if (!isLoading) onDismiss()
                }) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val shadowsocksInfo = parseShadowsocksUrl(serverKey)
                                onSave(serverName, serverKey, shadowsocksInfo)
                                isLoading = false
                            } catch (e: Exception) {
                                errorMessage = e.message
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(id = R.string.save))
                }

            }
        }
    )
}

@Preview
@Composable
fun DialogPrewiew() {
    ServerDialog(
        currentName = "Server Name",
        currentKey = "",
        onDismiss = {},
        onSave = { _, _, _ -> },
        onClear = {}
    )
}



