package app.android.outlinevpntv.utils.activityresult.base

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A `ActivityResultAPI` launcher specifying that an activity can be called with an input of type [I]
 * and produce an output of type [O].
 */
abstract class BaseLauncher<I, O>(
    private val contract: ActivityResultContract<I, O>
) : ActivityResultCallback<O>, DefaultLifecycleObserver {

    private val resultBuilder = ResultBuilder<O>()
    private lateinit var resultLauncher: ActivityResultLauncher<I>

    /**
     * Run result launch with callback
     */
    open fun launch(input: I, callbackBuilder: ResultBuilder<O>.() -> Unit) {
        resultBuilder.callbackBuilder()
        resultLauncher.launch(input)
    }

    /**
     * Register result launcher with [lifecycleOwner]
     */
    fun register(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    final override fun onCreate(owner: LifecycleOwner) {
        if (owner is ComponentActivity) {
            resultLauncher = owner.registerForActivityResult(contract, this)
        } else if (owner is Fragment) {
            resultLauncher = owner.registerForActivityResult(contract, this)
        }
    }

    final override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        resultLauncher.unregister()
    }

    override fun onActivityResult(result: O) {
        when {
            result != null -> resultBuilder.success.invoke(result)
            else -> resultBuilder.failed.invoke()
        }
    }
}

fun <O> BaseLauncher<Void?, O>.launch(callbackBuilder: ResultBuilder<O>.() -> Unit) {
    launch(null, callbackBuilder)
}

@JvmName("launchUnit")
fun <O> BaseLauncher<Unit, O>.launch(callbackBuilder: ResultBuilder<O>.() -> Unit) {
    launch(Unit, callbackBuilder)
}