package app.android.outlinevpntv.ui

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.android.outlinevpntv.R
import app.android.outlinevpntv.data.model.FileSorageOption
import java.io.File

@Composable
fun StoragePickerDialog(
    onFileSelected: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val internalRoot = context.filesDir
    val externalRoot = Environment.getExternalStorageDirectory()

    val storageOptions = listOfNotNull(
        FileSorageOption(stringResource(id = R.string.internal_storage), internalRoot),
        externalRoot?.let {
            if (it.exists() && it.canRead()) {
                FileSorageOption(stringResource(id = R.string.external_memory), it)
            } else null
        }
    )

    var selectedStorage by remember { mutableStateOf<FileSorageOption?>(null) }

    if (selectedStorage == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.select_outline_key),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                if (storageOptions.isEmpty()) {
                    Text(text = stringResource(id = R.string.could_not_devices))
                } else {
                    LazyColumn {
                        items(storageOptions) { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStorage = option
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    } else {
        FileBrowserDialog(
            rootDirectory = selectedStorage!!.file,
            onFileSelected = onFileSelected,
            onDismiss = onDismiss,
            onGoBack = {
                selectedStorage = null
            }
        )
    }
}



@Composable
fun FileBrowserDialog(
    rootDirectory: File,
    onFileSelected: (File) -> Unit,
    onDismiss: () -> Unit,
    onGoBack: () -> Unit,
) {
    var currentDirectory by remember { mutableStateOf(rootDirectory) }
    val directoryItems by remember(currentDirectory) {
        mutableStateOf(
            currentDirectory.listFiles()
                ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
                ?: emptyList()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (currentDirectory == rootDirectory) {
                            onGoBack()
                        } else {
                            currentDirectory.parentFile?.let { parent ->
                                if (parent.absolutePath.contains(rootDirectory.absolutePath)) {
                                    currentDirectory = parent
                                } else {
                                    currentDirectory = rootDirectory
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = currentDirectory.absolutePath,
                        fontSize = 14.sp
                    )
                }
            }
        },
        text = {
            if (directoryItems.isEmpty()) {
                Text(text = stringResource(id = R.string.there_are_nofiles) + " ${currentDirectory.path}")
            } else {
                LazyColumn {
                    items(directoryItems) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (file.isDirectory) {
                                        currentDirectory = file
                                    } else {
                                        onFileSelected(file)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = if (file.isDirectory) {
                                Icons.Default.Folder
                            } else {
                                Icons.Default.Article
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = file.name,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}