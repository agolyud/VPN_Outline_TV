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
            // Используем remember для сохранения состояний при изменениях
            val isConnected by viewModel.vpnState.observeAsState(false)
            val vpnStartTime = viewModel.getVpnStartTime()
            val ssUrl = remember { mutableStateOf(TextFieldValue(preferencesManager.getVpnKey() ?: "")) }
            val serverName = remember { mutableStateOf(preferencesManager.getServerName() ?: "Server Name") }
            val errorMessage = remember { mutableStateOf<String?>(null) }

            val scope = rememberCoroutineScope()

            MainScreen(
                isConnected = isConnected,
                ssUrl = ssUrl.value,
                serverName = serverName.value,
                vpnStartTime = vpnStartTime,
                onConnectClick = { ssUrlText ->
                    scope.launch {
                        try {
                            preferencesManager.saveVpnKey(ssUrlText)
                            ssUrl.value = TextFieldValue(ssUrlText)
                            startVpn { e ->
                                if (e != null) {
                                    errorMessage.value = e.localizedMessage
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage.value = e.localizedMessage
                        }
                    }
                },
                onDisconnectClick = {
                    viewModel.stopVpn(this)
                },
                onSaveServer = { newServerName, newVpnKey ->
                    serverName.value = newServerName
                    ssUrl.value = TextFieldValue(newVpnKey)
                    preferencesManager.saveServerName(newServerName)
                    preferencesManager.saveVpnKey(newVpnKey)
                }
            )

            errorMessage.value?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
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
                onError(e)
            }
        }
    }
}
