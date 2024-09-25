package app.android.outlinevpntv.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.android.outlinevpntv.R
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.android.outlinevpntv.viewmodel.ServerDialogViewModel


@SuppressLint("Recycle")
@Composable
fun ServerDialog(
    currentName: String,
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val viewModel: ServerDialogViewModel = viewModel(factory = ServerDialogViewModel.Factory)

    var serverName by remember { mutableStateOf(currentName) }
    var serverKey by remember { mutableStateOf(currentKey) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isKeyError by remember { mutableStateOf(false) }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun validateKey(key: String) {
        isKeyError = !viewModel.validate(key)
    }

    fun setServerKey(key: String) {
        // Validate key
        validateKey(key)
        // Try to read server name from connection URL
        serverName = key.substringAfterLast("#", serverName)
        serverKey = key
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val data = stream.reader().readText().trim()
                if (data.isNotBlank()) {
                    setServerKey(data)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.edit_server_info),
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val clipboardText = clipboardManager.getText()?.text
                        if (!clipboardText.isNullOrEmpty()) {
                            setServerKey(clipboardText)
                        } else {
                            Toast.makeText(context, R.string.clipboard_empty, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = "Paste from clipboard"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { filePicker.launch(arrayOf("text/*")) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = "Read from file"
                    )
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
                    isError = isKeyError,
                    supportingText = {
                        if (isKeyError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = context.getString(R.string.wrong_outline_key_format),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    onValueChange = { setServerKey(it) },
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
                    serverName = context.getString(R.string.default_server_name)
                    serverKey = ""
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
                        isLoading = true
                        try {
                            onSave(serverName, serverKey)
                            isLoading = false
                        } catch (e: Exception) {
                            errorMessage = e.message
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && !isKeyError
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )
}

@Preview
@Composable
fun DialogPreview() {
    ServerDialog(
        currentName = "Server #1",
        currentKey = "",
        onDismiss = {},
        onSave = { _, _ -> },
    )
}



