package com.example.vpnoutline

import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.vpnoutline.OutlineVpnService.Companion.HOST
import com.example.vpnoutline.OutlineVpnService.Companion.PORT
import com.example.vpnoutline.OutlineVpnService.Companion.PASSWORD
import com.example.vpnoutline.OutlineVpnService.Companion.METHOD
import java.nio.charset.StandardCharsets
import java.util.Base64


class MainActivity : ComponentActivity() {

    private lateinit var editTextSsUrl: EditText
    private lateinit var buttonSave: Button
    private val viewModel: MainViewModel by viewModels()
    private val vpnPreparation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> if (result.resultCode == RESULT_OK) viewModel.startVpn(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSave = findViewById(R.id.buttonSave)
        editTextSsUrl = findViewById(R.id.editTextSsUrl)

        buttonSave.setOnClickListener {
            val ssUrl = editTextSsUrl.text.toString()
            val shadowsocksInfo = parseShadowsocksUrl(ssUrl)

            println("HOST: ${shadowsocksInfo.host}")
            println("PORT: ${shadowsocksInfo.port}")
            println("PASSWORD: ${shadowsocksInfo.password}")
            println("METHOD: ${shadowsocksInfo.method}")


            HOST = shadowsocksInfo.host
            PORT = shadowsocksInfo.port
            PASSWORD = shadowsocksInfo.password
            METHOD = shadowsocksInfo.method

            startVpn()


            val webView: WebView = findViewById(R.id.webView)
            webView.settings.javaScriptEnabled = true

            // Устанавливаем URL веб-страницы
            val url = "https://whoer.net"
            webView.loadUrl(url)

            // Настройка WebViewClient для перехвата URL-адресов внутри приложения
            webView.webViewClient = WebViewClient()

            // Настройка WebChromeClient для поддержки дополнительных функций, например, заголовка страницы



        }
    }


    fun startVpn() = VpnService.prepare(this)?.let {
        vpnPreparation.launch(it)
    } ?: viewModel.startVpn(this)


    fun parseShadowsocksUrl(ssUrl: String): ShadowsocksInfo {
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

    fun decodeBase64(encoded: String): String {
        val decodedBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(encoded)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

    data class ShadowsocksInfo(val method: String, val password: String, val host: String, val port: Int)

    override fun onPause() {
        super.onPause()
        viewModel.stopVpn(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.startVpn(this)
    }

}

