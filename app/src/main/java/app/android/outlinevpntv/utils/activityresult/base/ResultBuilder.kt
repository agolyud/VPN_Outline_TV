package app.android.outlinevpntv.utils.activityresult.base

class ResultBuilder<I>(
    /**
     * Result loaded
     */
    var success: (result: I & Any) -> Unit = {},
    /**
     * Not loaded: Cancelled or Failed
     */
    var failed: () -> Unit = {}
)