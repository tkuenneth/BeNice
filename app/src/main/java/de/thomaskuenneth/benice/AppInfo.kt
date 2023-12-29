package de.thomaskuenneth.benice

import android.graphics.drawable.Drawable


data class AppInfo(
    val icon: Drawable,
    val label: String,
    val packageName: String,
    val className: String
)

sealed class InstalledAppsResult(
    data: List<AppInfo> = emptyList()
) {
    data object Loading : InstalledAppsResult()
    data class Success(val data: List<AppInfo>) : InstalledAppsResult(data)
}
