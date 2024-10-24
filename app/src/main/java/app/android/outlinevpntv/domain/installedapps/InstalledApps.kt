package app.android.outlinevpntv.domain.installedapps

import android.content.Context
import app.android.outlinevpntv.data.model.AppInfo

fun InstalledApps(requireContext: () -> Context, selectedApps: List<String>): List<AppInfo> {
    val pm = requireContext().packageManager
    return pm.getInstalledApplications(0)
        .filter { it.packageName != requireContext().packageName }
        .map {
            AppInfo(
                appName = pm.getApplicationLabel(it).toString(),
                packageName = it.packageName,
                icon = pm.getApplicationIcon(it.packageName),
                isSelected = selectedApps.contains(it.packageName)
            )
        }
        .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
}