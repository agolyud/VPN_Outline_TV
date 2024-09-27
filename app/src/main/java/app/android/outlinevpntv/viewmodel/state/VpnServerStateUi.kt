package app.android.outlinevpntv.viewmodel.state

data class VpnServerStateUi(
    val name: String,
    val host: String,
    val url: String,
    val startTime: Long = 0L,
) {
    companion object {
        val DEFAULT = VpnServerStateUi(name = "", host = "", url = "", startTime = 0L)
    }
}
