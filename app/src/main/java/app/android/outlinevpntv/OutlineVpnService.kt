package app.android.outlinevpntv

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.util.Log
import com.google.android.gms.fido.fido2.api.common.ErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import shadowsocks.Client
import shadowsocks.Config
import shadowsocks.Shadowsocks


class OutlineVpnService : VpnService() {

    companion object {
        var HOST = "127.0.0.1"
        var PORT = 34675
        var PASSWORD = "password"
        var METHOD = "Method"
        private const val PREFIX = "\u0000\u0080\u00ff"

        private const val TAG = "OutlineVpnService"
        private const val ACTION_START = "action.start"
        private const val ACTION_STOP = "action.stop"

        private const val NOTIFICATION_CHANNEL_ID = "outline-vpn"
        private const val NOTIFICATION_COLOR = 0x00BFA5
        private const val NOTIFICATION_SERVICE_ID = 1


        private var isRunning = false

        fun isVpnConnected(): Boolean {
            return isRunning
        }

        fun start(context: Context) {
            context.startService(newIntent(context, ACTION_START))
        }

        fun stop(context: Context) {
            context.startService(newIntent(context, ACTION_STOP))
        }

        private fun newIntent(context: Context, action: String): Intent {
            return Intent(context, OutlineVpnService::class.java).apply {
                this.action = action
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var vpnTunnel: VpnTunnel

    private var notificationBuilder: Notification.Builder? = null

    override fun onCreate() {
        Log.i(TAG, "onCreate: ")
        vpnTunnel = VpnTunnel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        return when {
            action == ACTION_START && !isRunning -> {
                startVpn()
                START_STICKY
            }
            action == ACTION_STOP -> {
                stopVpn()
                START_NOT_STICKY
            }
            else -> START_STICKY
        }
    }

    private fun startVpn() = scope.launch(Dispatchers.IO) {
        val isAutoStart = false

        val configCopy = Config().apply {
            host = HOST
            port = PORT.toLong()
            cipherName = METHOD
            password = PASSWORD
        }

        val client = try {
            Client(configCopy)
        } catch (e: Exception) {
            Log.i(TAG, "startVpn: Invalid configuration")
            return@launch
        }

        if (!isAutoStart) {
            try {
                val errorCode = checkServerConnectivity(client)
                if (errorCode != ErrorCode.NO_ERROR && errorCode != ErrorCode.UDP_RELAY_NOT_ENABLED) {
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(TAG, "startVpn: SHADOWSOCKS_START_FAILURE", e)
                return@launch
            }
        }

        if (!vpnTunnel.establishVpn()) {
            Log.i(TAG, "startVpn: Failed to establish the VPN")
            return@launch
        }

        val remoteUdpForwardingEnabled = false
        try {
            vpnTunnel.connectTunnel(client, remoteUdpForwardingEnabled)
            isRunning = true // Установка isRunning после успешного подключения
            startForegroundWithNotification()
        } catch (e: Exception) {
            Log.e(TAG, "startVpn: Failed to connect the tunnel", e)
            isRunning = false
        }
    }

    private fun stopVpn() {
        stopVpnTunnel()
        stopForeground()
        stopSelf()
        isRunning = false
    }

    private fun checkServerConnectivity(client: Client): ErrorCode {
        return try {
            val errorCode = Shadowsocks.checkConnectivity(client)
            val result: ErrorCode = ErrorCode.values()[errorCode.toInt()]
            Log.i(TAG, "checkServerConnectivity: Go connectivity check result: ${result.name}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "checkServerConnectivity: Connectivity checks failed", e)
            ErrorCode.UNEXPECTED
        }
    }

    private fun startForegroundWithNotification() {
        try {
            if (notificationBuilder == null) {

                notificationBuilder = getNotificationBuilder()
            }
            notificationBuilder!!.setContentText("outline_vpn")
            startForeground(
                NOTIFICATION_SERVICE_ID,
                notificationBuilder!!.build()
            )
        } catch (e: java.lang.Exception) {
            Log.e(
                TAG,
                "startForegroundWithNotification: Unable to display persistent notification",
                e
            )
        }
    }

    @Throws(Exception::class)
    private fun getNotificationBuilder(): Notification.Builder? {
        val launchIntent = Intent(this, getPackageMainActivityClass())
        val mainActivityIntent =
            PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Outline",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder.setContentTitle("outline_vpn")
            .setColor(NOTIFICATION_COLOR)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .setContentIntent(mainActivityIntent)
            .setShowWhen(true)
            .setUsesChronometer(true)
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

    @Throws(Exception::class)
    private fun getPackageMainActivityClass(): Class<*>? {
        return try {
            Class.forName("$packageName.MainActivity")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun stopVpnTunnel() {
        vpnTunnel.disconnectTunnel()
        vpnTunnel.tearDownVpn()
    }

    private fun stopForeground() {
        stopForeground(true)
        notificationBuilder = null
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