package app.android.outlinevpntv.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.android.outlinevpntv.viewmodel.state.VpnServerStateUi
import app.android.outlinevpntv.viewmodel.state.SingleLiveEvent
import app.android.outlinevpntv.utils.versionName
import app.android.outlinevpntv.R
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isConnected: Boolean,
    errorEvent: LiveData<Unit>,
    vpnServerState: VpnServerStateUi,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit,
    onSaveServer: (String, String) -> Unit,
) {
    val errorMessage by remember { mutableStateOf<String?>(null) }
    var elapsedTime by remember { mutableIntStateOf(0) }
    val isEditing by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isConnectionLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Error events observer
    LaunchedEffect(Unit) {
        errorEvent.observe(lifecycleOwner) {
            isConnectionLoading = false
            Toast.makeText(
                context,
                context.getString(R.string.vpn_start_failed),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    // Connection time
    LaunchedEffect(isConnected, vpnServerState.startTime) {
        isConnectionLoading = false
        while (isConnected) {
            delay(1000L)
            elapsedTime = ((System.currentTimeMillis() - vpnServerState.startTime) / 1000).toInt()
        }
        elapsedTime = 0
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
                            text = context.getString(R.string.version) + " " + versionName(context),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = context.getString(R.string.by_author),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.getString(R.string.github_link))
                            )
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {}
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
                serverName = vpnServerState.name,
                serverHost = vpnServerState.host,
                onForwardIconClick = {
                    if (!isConnected && !isConnectionLoading) {
                        isDialogOpen = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.disconnect_before_settings),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            if (isDialogOpen) {
                ServerDialog(
                    currentName = vpnServerState.name,
                    currentKey = vpnServerState.url,
                    onDismiss = { isDialogOpen = false },
                    onSave = { name, key ->
                        onSaveServer(name, key)
                        isDialogOpen = false
                    },
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isConnectionLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(120.dp).padding(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(30.dp))
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
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(true)
                        ) {
                            if (!isEditing) {
                                isConnectionLoading = true
                                if (isConnected) {
                                    onDisconnectClick()
                                } else {
                                    onConnectClick(vpnServerState.url)
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
                            text = context.getString(if (isConnected) R.string.off else R.string.on),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isConnected) {
                Text(
                    text = context.getString(R.string.elapsed_time),
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
            }

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
        isConnected = false,
        errorEvent = SingleLiveEvent(),
        vpnServerState = VpnServerStateUi(
            name = "Server #1",
            host = "172.66.44.135:80",
            url = "ss://5df7962e-f9fe-41e6-ab49-ed96ccb856a7@172.66.44.135:80?path=%2F&security=none&encryption=none&host=v2ra1.ecrgpk.workers.dev&type=ws#United States%20#1269%20/%20OutlineKeys.com"
        ),
        onConnectClick = {_-> },
        onDisconnectClick = {},
        onSaveServer = {_,_ -> },
    )
}


