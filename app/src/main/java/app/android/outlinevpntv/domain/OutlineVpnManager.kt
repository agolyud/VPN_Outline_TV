package app.android.outlinevpntv.domain

import android.content.Context
import app.android.outlinevpntv.data.model.ShadowSocksInfo
import app.android.outlinevpntv.data.preferences.PreferencesManager

class OutlineVpnManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private  var vpnTunnel: VpnTunnel

    init {
        vpnTunnel = VpnTunnel(OutlineVpnService(), preferencesManager)
    }

    fun start(config: ShadowSocksInfo) = OutlineVpnService.start(context, config)

    fun stop() = OutlineVpnService.stop(context)

    fun isConnected() = OutlineVpnService.isVpnConnected()

    fun establishVpn(): Boolean {
        return vpnTunnel.establishVpn()
    }
}