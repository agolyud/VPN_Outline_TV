package app.android.outlinevpntv.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UpdateDialog(onUpdate: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Обновление доступно") },
        text = { Text("Хотите обновить приложение?") },
        confirmButton = {
            TextButton(onClick = {
                onUpdate()
                onDismiss()
            }) {
                Text("Обновить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewUpdateDialog() {
    UpdateDialog(
        onUpdate = {},
        onDismiss = {}
    )
}
