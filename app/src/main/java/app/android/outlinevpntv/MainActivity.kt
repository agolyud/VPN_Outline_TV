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
import androidx.compose.ui.text.input.TextFieldValue
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

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        // Проверяем состояние VPN при запуске активности
        checkVpnState()

        setContent {
            val isConnected by viewModel.vpnState.observeAsState(false)
            val vpnStartTime = viewModel.getVpnStartTime()
            var ssUrl by remember { mutableStateOf(TextFieldValue(preferencesManager.getVpnKey() ?: "")) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            MainScreen(
                isConnected = isConnected,
                ssUrl = ssUrl,
                vpnStartTime = vpnStartTime,
                onConnectClick = { ssUrlText ->
                    try {
                        val shadowsocksInfo = parseShadowsocksUrl(ssUrlText)
                        preferencesManager.saveVpnKey(ssUrlText)
                        ssUrl = TextFieldValue(ssUrlText)
                        HOST = shadowsocksInfo.host
                        PORT = shadowsocksInfo.port
                        PASSWORD = shadowsocksInfo.password
                        METHOD = shadowsocksInfo.method
                        viewModel.startVpn(this)
                    } catch (e: IllegalArgumentException) {
                        errorMessage = e.message
                    } catch (e: Exception) {
                        errorMessage = e.localizedMessage
                    }
                },
                onDisconnectClick = {
                    viewModel.stopVpn(this)
                }
            )

            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkVpnState()
    }

    private fun checkVpnState() {
        val isVpnConnected = OutlineVpnService.isVpnConnected()
        viewModel.setVpnState(isVpnConnected)
    }

    private fun startVpn(onError: (Exception?) -> Unit) {
        val preparationIntent = VpnService.prepare(this)
        if (preparationIntent != null) {
            vpnPreparation.launch(preparationIntent)
        } else {
            try {
                viewModel.startVpn(this)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun parseShadowsocksUrl(ssUrl: String): ShadowsocksInfo {
        val regex = Regex("ss://([^@]+)@([^:]+):(\\d+)(?:[#/?]?.*)?")
        val matchResult = regex.find(ssUrl)
        if (matchResult != null) {
            val groups = matchResult.groupValues
            val encodedInfo = groups[1]
            val decodedInfo = decodeBase64(encodedInfo)

            val host = groups[2]
            val port = groups[3].toInt()

            val parts = decodedInfo.split(":")
            if (parts.size != 2) {
                throw IllegalArgumentException(getString(R.string.invalid_decoded_info_format))
            }
            val method = parts[0]
            val password = parts[1]

            return ShadowsocksInfo(method, password, host, port)
        } else {
            throw IllegalArgumentException(getString(R.string.invalid_link_format))
        }
    }

    private fun decodeBase64(encoded: String): String {
        val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

    data class ShadowsocksInfo(val method: String, val password: String, val host: String, val port: Int)
}
