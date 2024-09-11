package app.android.outlinevpntv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.android.outlinevpntv.data.model.ShadowsocksInfo
import app.android.outlinevpntv.data.remote.parseShadowsocksUrl
import kotlinx.coroutines.launch

@Composable
fun ServerDialog(
    currentName: String,
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, ShadowsocksInfo?) -> Unit
) {
    var serverName by remember { mutableStateOf(currentName) }
    var serverKey by remember { mutableStateOf(currentKey) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Server Info")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("Server Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = serverKey,
                    onValueChange = { serverKey = it },
                    label = { Text("Server Key") }
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    try {
                        val shadowsocksInfo = parseShadowsocksUrl(serverKey)
                        onSave(serverName, serverKey, shadowsocksInfo)
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}