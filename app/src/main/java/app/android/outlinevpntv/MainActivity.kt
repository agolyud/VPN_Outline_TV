package app.android.outlinevpntv

import android.net.VpnService
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import app.android.outlinevpntv.OutlineVpnService.Companion.HOST
import app.android.outlinevpntv.OutlineVpnService.Companion.METHOD
import app.android.outlinevpntv.OutlineVpnService.Companion.PASSWORD
import app.android.outlinevpntv.OutlineVpnService.Companion.PORT
import app.android.outlinevpntv.ui.MainScreen
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val vpnPreparation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> if (result.resultCode == RESULT_OK) viewModel.startVpn(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isConnected by viewModel.vpnState.observeAsState(false)

            MainScreen(
                isConnected = isConnected,
                onConnectClick = { ssUrl ->
                    try {
                        val shadowsocksInfo = parseShadowsocksUrl(ssUrl)
                        HOST = shadowsocksInfo.host
                        PORT = shadowsocksInfo.port
                        PASSWORD = shadowsocksInfo.password
                        METHOD = shadowsocksInfo.method
                        startVpn()
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                },
                onDisconnectClick = {
                    viewModel.stopVpn(this)
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




