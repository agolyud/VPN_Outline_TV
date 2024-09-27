package app.android.outlinevpntv.domain

import android.content.Context
import app.android.outlinevpntv.data.model.ShadowSocksInfo

class OutlineVpnManager(private val context: Context) {
    fun start(config: ShadowSocksInfo) = OutlineVpnService.start(context, config)
    fun stop() = OutlineVpnService.stop(context)
    fun isConnected() = OutlineVpnService.isVpnConnected()
}