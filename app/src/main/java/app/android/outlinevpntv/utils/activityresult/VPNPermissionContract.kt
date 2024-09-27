package app.android.outlinevpntv.utils.activityresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract

class VPNPermissionContract : ActivityResultContract<Void?, Boolean>() {
    private var cachedIntent: Intent? = null

    override fun getSynchronousResult(context: Context, input: Void?): SynchronousResult<Boolean>? {
        VpnService.prepare(context)?.let { intent ->
            cachedIntent = intent
            return null
        }
        return SynchronousResult(true)
    }

    override fun createIntent(context: Context, input: Void?) = cachedIntent!!.also { cachedIntent = null }

    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
}