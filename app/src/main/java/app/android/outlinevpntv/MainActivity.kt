package app.android.outlinevpntv

import android.net.VpnService
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.android.outlinevpntv.OutlineVpnService.Companion.HOST
import app.android.outlinevpntv.OutlineVpnService.Companion.METHOD
import app.android.outlinevpntv.OutlineVpnService.Companion.PASSWORD
import app.android.outlinevpntv.OutlineVpnService.Companion.PORT
import app.android.outlinevpntv.ui.theme.OutlineVPNtvTheme
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val vpnPreparation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> if (result.resultCode == RESULT_OK) viewModel.startVpn(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isConnected by remember { mutableStateOf(false) }

            MainScreen(
                isConnected = isConnected,
                onConnectClick = { ssUrl ->
                    val shadowsocksInfo = parseShadowsocksUrl(ssUrl)
                    HOST = shadowsocksInfo.host
                    PORT = shadowsocksInfo.port
                    PASSWORD = shadowsocksInfo.password
                    METHOD = shadowsocksInfo.method
                    startVpn()
                    isConnected = true
                },
                onDisconnectClick = {
                    viewModel.stopVpn(this)
                    isConnected = false
                }
            )
        }
    }

    private fun startVpn() = VpnService.prepare(this)?.let {
        vpnPreparation.launch(it)
    } ?: viewModel.startVpn(this)

    private fun parseShadowsocksUrl(ssUrl: String): ShadowsocksInfo {
        val regex = Regex("ss://(.*?)@(.*):(\\d+)")
        val matchResult = regex.find(ssUrl)
        if (matchResult != null) {
            val groups = matchResult.groupValues
            val encodedInfo = groups[1]
            val decodedInfo = decodeBase64(encodedInfo)
            val parts = decodedInfo.split(":")
            val method = parts[0]
            val password = parts[1]
            val host = groups[2]
            val port = groups[3].toInt()

            return ShadowsocksInfo(method, password, host, port)
        } else {
            throw IllegalArgumentException("Неверный формат ссылки Outline")
        }
    }

    private fun decodeBase64(encoded: String): String {
        val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

    data class ShadowsocksInfo(val method: String, val password: String, val host: String, val port: Int)
}


@Composable
fun MainScreen(
    isConnected: Boolean,
    onConnectClick: (String) -> Unit,
    onDisconnectClick: () -> Unit
) {
    var ssUrl by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = ssUrl,
            onValueChange = { ssUrl = it },
            label = { Text("Тут ключ") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (isConnected) {
                    onDisconnectClick()
                } else {
                    onConnectClick(ssUrl.text)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isConnected) "Отключиться" else "Подключиться")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen(
        onConnectClick = {},
        onDisconnectClick = {},
        isConnected = false
    )
}
