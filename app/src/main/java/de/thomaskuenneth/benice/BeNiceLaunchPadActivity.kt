package de.thomaskuenneth.benice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

private const val ACTION_LAUNCH_APP = "de.thomaskuenneth.benice.intent.action.ACTION_LAUNCH_APP"
private const val PACKAGE_NAME = "packageName"
private const val CLASS_NAME = "className"

class BeNiceLaunchPadActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()
        Intent(this, BeNiceActivity::class.java).run {
            startActivity(this)
            Handler(Looper.getMainLooper()).postDelayed({
                launchApp(intent)
            }, 500L)
        }
    }

    private fun launchApp(intent: Intent) {
        lifecycleScope.launch {
            if (ACTION_LAUNCH_APP == intent.action) {
                intent.getStringExtra(PACKAGE_NAME)?.let { packageName ->
                    intent.getStringExtra(CLASS_NAME)?.let { className ->
                        launchApp(
                            packageName = packageName,
                            className = className
                        )
                    }
                }
            }
        }
    }
}

fun Context.launchApp(packageName: String, className: String) {
    Intent().run {
        component = ComponentName(
            packageName,
            className
        )
        addFlags(
            Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        )
        startActivity(this)
    }
}

fun Context.createBeNiceLaunchIntent(appInfo: AppInfo) =
    Intent(this, BeNiceLaunchPadActivity::class.java).also { intent ->
        intent.action = ACTION_LAUNCH_APP
        intent.putExtra(PACKAGE_NAME, appInfo.packageName)
        intent.putExtra(CLASS_NAME, appInfo.className)
    }
