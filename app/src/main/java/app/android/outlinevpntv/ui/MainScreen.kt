package app.android.outlinevpntv.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import app.android.outlinevpntv.domain.OutlineVpnService.Companion.HOST
import app.android.outlinevpntv.domain.OutlineVpnService.Companion.METHOD
import app.android.outlinevpntv.domain.OutlineVpnService.Companion.PASSWORD
import app.android.outlinevpntv.domain.OutlineVpnService.Companion.PORT
import app.android.outlinevpntv.R
import app.android.outlinevpntv.data.preferences.PreferencesManager
import app.android.outlinevpntv.utils.versionName
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isConnected: Boolean,
    ssUrl: TextFieldValue,
    serverName: String,
    preferencesManager: PreferencesManager,
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
                serverName = serverNameState,
                serverIp = ssUrlState.text,
                hostIp = HOST,
                onForwardIconClick = { isDialogOpen = true },
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
                    },
                    onClear = {
                        preferencesManager.clearVpnStartTime()
                        preferencesManager.saveServerName("Server Name")
                        preferencesManager.saveVpnKey("")
                        serverNameState = "Server Name"
                        ssUrlState = TextFieldValue("")
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



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen(
        onConnectClick = {},
        onDisconnectClick = {},
        ssUrl = TextFieldValue("ss://5df7962e-f9fe-41e6-ab49-ed96ccb856a7@172.66.44.135:80?path=%2F&security=none&encryption=none&host=v2ra1.ecrgpk.workers.dev&type=ws#United States%20#1269%20/%20OutlineKeys.com"),
        isConnected = false,
        preferencesManager = PreferencesManager(LocalContext.current),
        serverName = "Server Name",
        onSaveServer = { _, _ -> },
        vpnStartTime = 0
    )
}


