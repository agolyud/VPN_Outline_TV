package app.android.outlinevpntv

import android.net.VpnService
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val vpnPreparation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) viewModel.startVpn(this)
    }

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        checkVpnState()

        setContent {
            val isConnected by viewModel.vpnState.observeAsState(false)
            val vpnStartTime = viewModel.getVpnStartTime()
            var ssUrl by remember { mutableStateOf(TextFieldValue(preferencesManager.getVpnKey() ?: "")) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            MainScreen(
                isConnected = isConnected,
                ssUrl = ssUrl,
                vpnStartTime = vpnStartTime,
                onConnectClick = { ssUrlText ->
                    scope.launch {
                        try {
                            val shadowsocksInfo = parseShadowsocksUrl(ssUrlText)
                            preferencesManager.saveVpnKey(ssUrlText)
                            ssUrl = TextFieldValue(ssUrlText)
                            HOST = shadowsocksInfo.host
                            PORT = shadowsocksInfo.port
                            PASSWORD = shadowsocksInfo.password
                            METHOD = shadowsocksInfo.method
                            startVpn { e ->
                                if (e != null) {
                                    Log.e("VPN", "Error starting VPN", e)
                                    errorMessage = e.localizedMessage
                                }
                            }
                        } catch (e: IllegalArgumentException) {
                            Log.e("VPN", "Invalid argument: ${e.message}")
                            errorMessage = e.message
                        } catch (e: Exception) {
                            Log.e("VPN", "Exception: ${e.localizedMessage}")
                            errorMessage = e.localizedMessage
                        }
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

    private fun startVpn(onError: (Exception?) -> Unit = {}) {
        val preparationIntent = VpnService.prepare(this)
        if (preparationIntent != null) {
            vpnPreparation.launch(preparationIntent)
        } else {
            try {
                viewModel.startVpn(this)
            } catch (e: Exception) {
                Log.e("VPN", "Error starting VPN", e)
                onError(e)
            }
        }
    }


    private suspend fun parseShadowsocksUrl(ssUrl: String): ShadowsocksInfo {
        Log.d("VPN", "Parsing URL: $ssUrl")
        return if (ssUrl.startsWith("ssconf://")) {
            parseShadowsocksConfUrl(ssUrl)
        } else if (ssUrl.startsWith("ss://")) {
            parseShadowsocksSsUrl(ssUrl)
        } else {
            throw IllegalArgumentException(getString(R.string.invalid_link_format))
        }
    }


    private suspend fun parseShadowsocksConfUrl(ssConfUrl: String): ShadowsocksInfo = withContext(Dispatchers.IO) {

        if (!ssConfUrl.startsWith("ssconf://")) {
            throw IllegalArgumentException("Invalid ssconf URL format")
        }

        val urlWithoutFragment = ssConfUrl.split("#")[0]
        val httpsUrl = urlWithoutFragment.replace("ssconf://", "https://")
        val jsonResponse = fetchJsonFromUrl(httpsUrl)
        val jsonObject = JSONObject(jsonResponse)
        val host = jsonObject.getString("server")
        val portString = jsonObject.getString("server_port")
        val port = portString.toInt()  // Преобразуем порт из строки в Int
        val password = jsonObject.getString("password")  // Пароль используем как есть, без декодирования
        val method = jsonObject.getString("method")

        ShadowsocksInfo(method, password, host, port)
    }


    private suspend fun fetchJsonFromUrl(urlString: String): String = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connect()

            val responseCode = connection.responseCode

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch data, HTTP response code: $responseCode")
            }

            val inputStream = connection.inputStream
            inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun parseShadowsocksSsUrl(ssUrl: String): ShadowsocksInfo = withContext(Dispatchers.IO) {
        val regex = Regex("ss://([^@]+)@([^:]+):(\\d+)(?:/?.*)?")
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

            ShadowsocksInfo(method, password, host, port)
        } else {
            throw IllegalArgumentException(getString(R.string.invalid_link_format))
        }
    }


    private fun decodeBase64(encoded: String): String {
        return try {
            val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e("VPN", "Error decoding Base64: ${e.message}")
            throw e
        }
    }

    data class ShadowsocksInfo(val method: String, val password: String, val host: String, val port: Int)
}
