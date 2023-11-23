package com.example.vpnoutline

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
        var PORT = 11111
        var PASSWORD = "password"
        var METHOD = "Method"
        private const val PREFIX = "\u0000\u0080\u00ff"

        private const val TAG = "OutlineVpnService"
        private const val ACTION_START = "action.start"
        private const val ACTION_STOP = "action.stop"

        private const val NOTIFICATION_CHANNEL_ID = "outline-vpn"
        private const val NOTIFICATION_COLOR = 0x00BFA5
        private const val NOTIFICATION_SERVICE_ID = 1


        private var shadowsocksInfo: MainActivity.ShadowsocksInfo? = null

        fun setShadowsocksInfo(info: MainActivity.ShadowsocksInfo?) {
            shadowsocksInfo = info
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

    private var isRunning = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var vpnTunnel: VpnTunnel

    private var notificationBuilder: Notification.Builder? = null

    override fun onCreate() {
        Log.i(TAG, "onCreate: ")
        vpnTunnel = VpnTunnel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")
        val action = intent?.action
        return when {
            action == ACTION_START && !isRunning -> {
                start()
                isRunning = true
                START_STICKY
            }

            action == ACTION_STOP -> {
                stop()
                START_NOT_STICKY
            }

            else -> START_STICKY
        }
    }

    private fun start() = scope.launch(Dispatchers.IO) {
        val isAutoStart = false

        val configCopy = Config()
        configCopy.host = HOST
        configCopy.port = PORT.toLong()
        configCopy.cipherName = METHOD
        configCopy.password = PASSWORD
        //configCopy.prefix = PREFIX.toByteArray()

        val client = try {
            Client(configCopy)
        } catch (e: Exception) {
            Log.i(TAG, "start: Invalid configuration")
            return@launch
        }

        if (!isAutoStart) {
            try {
                // Do not perform connectivity checks when connecting on startup. We should avoid failing
                // the connection due to a network error, as network may not be ready.
                val errorCode = checkServerConnectivity(client)
                if (!(errorCode == ErrorCode.NO_ERROR || errorCode == ErrorCode.UDP_RELAY_NOT_ENABLED)) {
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(TAG, "start: SHADOWSOCKS_START_FAILURE", e)
                return@launch
            }
        }

        // Only establish the VPN if this is not a tunnel restart.
        if (!vpnTunnel.establishVpn()) {
            Log.i(TAG, "start: Failed to establish the VPN")
            return@launch
        }

        val remoteUdpForwardingEnabled = false
        try {
            vpnTunnel.connectTunnel(client, remoteUdpForwardingEnabled)
        } catch (e: Exception) {
            Log.e(TAG, "start: Failed to connect the tunnel", e)
            return@launch
        }
        startForegroundWithNotification()
    }

    // Shadowsocks
    private fun checkServerConnectivity(client: Client): ErrorCode {
        try {
            val errorCode = Shadowsocks.checkConnectivity(client)
            val result: ErrorCode = ErrorCode.values()[errorCode.toInt()]
            Log.i(TAG, "checkServerConnectivity: Go connectivity check result: ${result.name}")
            return result
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "checkServerConnectivity: Connectivity checks failed", e)
        }
        return ErrorCode.UNEXPECTED
    }

    // Foreground service & notifications
    private fun startForegroundWithNotification() {
        try {
            if (notificationBuilder == null) {
                // Cache the notification builder so we can update the existing notification - creating a
                // new notification has the side effect of resetting the tunnel timer.
                notificationBuilder = getNotificationBuilder()
            }
            notificationBuilder!!.setContentText("outline-go-tun2socks-demo")
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

    @Throws(java.lang.Exception::class)
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

        return builder.setContentTitle("outline-go-tun2socks-demo")
            .setColor(NOTIFICATION_COLOR)
            .setVisibility(Notification.VISIBILITY_SECRET) // Don't display in lock screen
            .setContentIntent(mainActivityIntent)
            .setShowWhen(true)
            .setUsesChronometer(true)
    }

    @Throws(java.lang.Exception::class)
    private fun getPackageMainActivityClass(): Class<*>? {
        return try {
            Class.forName("$packageName.MainActivity")
        } catch (e: java.lang.Exception) {
            throw e
        }
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

    fun newBuilder(): Builder {
        return Builder()
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getApplicationName(): String {
        val packageManager = applicationContext.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        return packageManager.getApplicationLabel(appInfo) as String
    }

    private fun stop() {
        stopVpnTunnel()
        stopForeground()
        stopSelf()
    }

    private fun stopVpnTunnel() {
        vpnTunnel.disconnectTunnel()
        vpnTunnel.tearDownVpn()
    }

    private fun stopForeground() {
        stopForeground(true /* remove notification */)
        notificationBuilder = null
    }

    override fun onRevoke() {
        super.onRevoke()
        Log.i(TAG, "onRevoke: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
    }
}