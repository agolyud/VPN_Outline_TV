package app.android.outlinevpntv.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import app.android.outlinevpntv.MainActivity
import app.android.outlinevpntv.R
import app.android.outlinevpntv.data.broadcast.BroadcastVpnServiceAction
import app.android.outlinevpntv.data.model.ShadowSocksInfo
import app.android.outlinevpntv.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import shadowsocks.Client
import shadowsocks.Config
import shadowsocks.Shadowsocks


class OutlineVpnService : VpnService() {

    companion object {
        private const val CONFIG_EXTRA = "OutlineVpnService:config"

        private const val TAG = "OutlineVpnService"
        private const val ACTION_START = "action.start"
        private const val ACTION_STOP = "action.stop"

        private const val NOTIFICATION_CHANNEL_ID = "outline-vpn"
        private const val NOTIFICATION_CHANNEL_NAME = "Outline"
        private const val NOTIFICATION_COLOR = 0x00BFA5
        private const val NOTIFICATION_SERVICE_ID = 1

        private lateinit var preferencesManager: PreferencesManager
        private var isRunning = false

        fun isVpnConnected(): Boolean {
            return isRunning
        }

        fun start(context: Context, config: ShadowSocksInfo) {
            context.startService(newIntent(context, ACTION_START).putExtra(CONFIG_EXTRA, config))
        }

        fun stop(context: Context) {
            context.startService(newIntent(context, ACTION_STOP))
        }

        private fun newIntent(context: Context, action: String): Intent {
            return Intent(context, OutlineVpnService::class.java).apply { this.action = action }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var vpnTunnel: VpnTunnel

    override fun onCreate() {

        preferencesManager = PreferencesManager(applicationContext)

        Log.i(TAG, "onCreate: ")
        registerNotificationChannel()
        vpnTunnel = VpnTunnel(this, preferencesManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        return when {
            action == ACTION_START && !isRunning -> {
                startVpn(intent.extras?.getParcelable<ShadowSocksInfo>(CONFIG_EXTRA))
                START_STICKY
            }
            action == ACTION_STOP -> {
                stopVpn()
                START_NOT_STICKY
            }
            else -> START_STICKY
        }
    }

    private fun startVpn(config: ShadowSocksInfo?) = scope.launch(Dispatchers.IO) {
        if (config == null) {
            Log.e(TAG, "startVpn: null config")
            sendBroadcast(
                Intent(BroadcastVpnServiceAction.ERROR)
            )
            return@launch
        }

        val ssConfig = Config().apply {
            host = config.host
            port = config.port.toLong()
            cipherName = config.method
            password = config.password
            prefix = config.prefix?.toByteArray()
        }

        val started = startVpnInternal(ssConfig)
        sendBroadcast(
            Intent(
                if (started) BroadcastVpnServiceAction.STARTED
                else BroadcastVpnServiceAction.ERROR
            )
        )
    }

    private fun startVpnInternal(config: Config): Boolean {
        val isAutoStart = false

        Log.d(TAG, "startVpn: Config -> $config")

        val client = try {
            Client(config)
        } catch (e: Exception) {
            Log.i(TAG, "startVpn: Invalid configuration", e)
            return false
        }

        Log.d(TAG, "startVpn: Shadowsocks Client created")

        if (!isAutoStart) {
            try {
                val errorCode = checkServerConnectivity(client)
                if (errorCode != ErrorCode.NO_ERROR && errorCode != ErrorCode.UDP_RELAY_NOT_ENABLED) {
                    Log.i(TAG, "startVpn: Server connectivity check failed with error $errorCode")
                    return false
                }
            } catch (e: Exception) {
                Log.e(TAG, "startVpn: SHADOWSOCKS_START_FAILURE", e)
                return false
            }
        }

        Log.d(TAG, "startVpn: Establishing VPN tunnel...")

        if (!vpnTunnel.establishVpn()) {
            Log.i(TAG, "startVpn: Failed to establish the VPN")
            return false
        }

        val remoteUdpForwardingEnabled = false
        try {
            vpnTunnel.connectTunnel(client, remoteUdpForwardingEnabled)
            isRunning = true // Установка isRunning после успешного подключения
            Log.i(TAG, "startVpn: VPN tunnel established successfully")
            startForegroundWithNotification()
        } catch (e: Exception) {
            Log.e(TAG, "startVpn: Failed to connect the tunnel", e)
            isRunning = false
        }

        return isRunning
    }

    private fun stopVpn() {
        stopVpnTunnel()
        stopForeground()
        stopSelf()
        isRunning = false

        sendBroadcast(Intent(BroadcastVpnServiceAction.STOPPED))
    }

    private fun checkServerConnectivity(client: Client): ErrorCode {
        return try {
            val errorCode = Shadowsocks.checkConnectivity(client)
            val result: ErrorCode = ErrorCode.entries[errorCode.toInt()]
            Log.i(TAG, "checkServerConnectivity: Go connectivity check result: ${result.name}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "checkServerConnectivity: Connectivity checks failed", e)
            ErrorCode.UNEXPECTED
        }
    }

    private fun startForegroundWithNotification() {
        try {
            val notification: Notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_SERVICE_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(NOTIFICATION_SERVICE_ID, notification)
            }
        } catch (e: java.lang.Exception) {
            Log.e(
                TAG,
                "startForegroundWithNotification: Unable to display persistent notification",
                e
            )
        }
    }

    private fun registerNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setSilent(true)
            .setColor(NOTIFICATION_COLOR)
            .setContentTitle(getString(R.string.vpn_name))
            .setContentText(getString(R.string.vpn_connected))
            .addAction(0, getString(R.string.stop_vpn),
                PendingIntent.getService(
                    this,
                    0,
                    Intent(this, OutlineVpnService::class.java).setAction(ACTION_STOP),
                    PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    // Plugin error codes. Keep in sync with www/model/errors.ts.
    enum class ErrorCode(val value: Int) {
        NO_ERROR(0),
        UNEXPECTED(1),
        VPN_PERMISSION_NOT_GRANTED(2),
        INVALID_SERVER_CREDENTIALS(3),
        UDP_RELAY_NOT_ENABLED(4),
        SERVER_UNREACHABLE(5),
        VPN_START_FAILURE(6),
        ILLEGAL_SERVER_CONFIGURATION(7),
        SHADOWSOCKS_START_FAILURE(8),
        CONFIGURE_SYSTEM_PROXY_FAILURE(9),
        NO_ADMIN_PERMISSIONS(10),
        UNSUPPORTED_ROUTING_TABLE(11),
        SYSTEM_MISCONFIGURED(12)
    }

    private fun stopVpnTunnel() {
        vpnTunnel.disconnectTunnel()
        vpnTunnel.tearDownVpn()
    }

    private fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.i(TAG, "onRevoke: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
        isRunning = false
    }

    fun newBuilder(): Builder {
        return Builder()
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getApplicationName(): String {
        val packageManager = applicationContext.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return packageManager.getApplicationLabel(appInfo) as String
    }
}