package app.android.outlinevpntv.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.android.outlinevpntv.MainActivity
import app.android.outlinevpntv.OutlineVpnService.Companion.HOST
import app.android.outlinevpntv.OutlineVpnService.Companion.METHOD
import app.android.outlinevpntv.OutlineVpnService.Companion.PASSWORD
import app.android.outlinevpntv.OutlineVpnService.Companion.PORT
import app.android.outlinevpntv.R
import app.android.outlinevpntv.ShadowsocksInfo
import app.android.outlinevpntv.parseShadowsocksUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isConnected: Boolean,
    ssUrl: TextFieldValue,
    serverName: String, // Добавляем параметр serverName
    vpnStartTime: Long,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit,
    onSaveServer: (String, String) -> Unit
) {
    var ssUrlState by remember { mutableStateOf(ssUrl) }
    var serverNameState by remember { mutableStateOf(serverName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var elapsedTime by remember { mutableStateOf(0) }
    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var serverName by remember { mutableStateOf("Server Name") }
    var isDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected, vpnStartTime) {
        if (isConnected && vpnStartTime > 0) {
            while (true) {
                delay(1000L)
                elapsedTime = ((System.currentTimeMillis() - vpnStartTime) / 1000).toInt()
            }
        } else {
            elapsedTime = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val waveHeight = height * 0.6f
            val wavePath = Path().apply {
                moveTo(0f, waveHeight)
                cubicTo(
                    width * 0.25f, waveHeight - 100f,
                    width * 0.75f, waveHeight + 100f,
                    width, waveHeight
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = wavePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFA0DEFF),
                        Color(0xFFFFF9D0)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            TopAppBar(
                title = {

                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = LocalContext.current.getString(R.string.version) + " " + versionName(context),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "by AlexGolyd",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/agolyud/VPN_Outline_TV"))
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
                            contentDescription = "Open GitHub",
                            tint = Color.Black
                        )
                    }
                }
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(130.dp)
                    .padding(top = 10.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(15.dp))

            ServerItem(
                serverImage = painterResource(id = R.drawable.logo),
                serverName = serverNameState,
                serverIp = ssUrlState.text,
                onForwardIconClick = { isDialogOpen = true }
            )

            if (isDialogOpen) {
                ServerDialog(
                    currentName = serverNameState,
                    currentKey = ssUrlState.text,
                    onDismiss = { isDialogOpen = false },
                    onSave = { newName, newKey, shadowsocksInfo ->
                        serverNameState = newName
                        ssUrlState = TextFieldValue(newKey)
                        onSaveServer(newName, newKey)
                        if (shadowsocksInfo != null) {
                            HOST = shadowsocksInfo.host
                            PORT = shadowsocksInfo.port
                            PASSWORD = shadowsocksInfo.password
                            METHOD = shadowsocksInfo.method
                        }
                        isDialogOpen = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isConnected) {
                                listOf(
                                    Color(0xFF5EFFB5),
                                    Color(0xFF2C7151)
                                )
                            } else {
                                listOf(
                                    Color(0xFFE57373),
                                    Color(0xFFFF8A65)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable {
                        if (!isEditing) {
                            try {
                                if (isConnected) {
                                    onDisconnectClick()
                                } else {
                                    // Используем уже сохраненные данные для подключения
                                    onConnectClick(ssUrlState.text)
                                }
                            } catch (e: IllegalArgumentException) {
                                errorMessage = e.message
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Crossfade(
                        targetState = isConnected,
                        animationSpec = tween(600),
                        label = "ConnectionStatusCrossfade"
                    ) { connected ->
                        Icon(
                            imageVector = if (connected) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text(
                        text = if (isConnected)
                            LocalContext.current.getString(R.string.off)
                        else LocalContext.current.getString(R.string.on),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = LocalContext.current.getString(R.string.elapsed_time),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    elapsedTime / 3600,
                    (elapsedTime % 3600) / 60,
                    elapsedTime % 60
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )

            errorMessage?.let { _message ->
                Text(
                    text = _message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }
    }
}

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

@Composable
fun ServerItem(
    serverImage: Painter,
    serverName: String,
    serverIp: String,
    onForwardIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clickable(onClick = onForwardIconClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = serverImage,
                contentDescription = "Server Image",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = serverName,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = serverIp,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "Forward Arrow",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun versionName(context: Context): String {
    return try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen(
        onConnectClick = {},
        onDisconnectClick = {},
        ssUrl = TextFieldValue(""),
        isConnected = false,
        serverName = "Server Name",
        onSaveServer = { _, _ -> },
        vpnStartTime = 0
    )
}


