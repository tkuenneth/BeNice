package de.thomaskuenneth.benice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Patterns
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri


data class AppInfo(
    val icon: Drawable,
    val label: String,
    val packageName: String,
    val className: String
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
                        className = info.activityInfo.name
                    )
                )
            }
        }
    }
}

fun Context.appVersion(): String = with(packageManager.getPackageInfo(packageName, 0)) {
    "$versionName (${PackageInfoCompat.getLongVersionCode(this)})"
}

fun String.isValidUrl(): Boolean {
    return Patterns.WEB_URL.matcher(this).matches()
}

fun sameApp(app1: AppInfo?, app2: AppInfo?): Boolean =
    (app1?.packageName ?: "") == (app2?.packageName ?: "")

fun String.createLabelFromURL(defaultStr: String): String = this.toUri().host ?: defaultStr
