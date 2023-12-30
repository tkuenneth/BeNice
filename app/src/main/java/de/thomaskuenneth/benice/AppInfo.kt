package de.thomaskuenneth.benice

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable


data class AppInfo(
    val icon: Drawable,
    val label: String,
    val packageName: String,
    val className: String,
    val screenOrientationPortrait: Boolean
)

fun installedApps(packageManager: PackageManager): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    return mutableListOf<AppInfo>().also { list ->
        packageManager.queryIntentActivities(intent, 0).forEach { info ->
            // filtering our app because nesting doesn't work, so it makes no sense to open it
            if (BuildConfig.APPLICATION_ID != info.activityInfo.packageName) {
                list.add(
                    AppInfo(
                        icon = info.activityInfo.loadIcon(packageManager),
                        label = info.activityInfo.loadLabel(packageManager).toString(),
                        packageName = info.activityInfo.packageName,
                        className = info.activityInfo.name,
                        screenOrientationPortrait = info.activityInfo.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    )
                )
            }
        }
    }
}
