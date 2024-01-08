package de.thomaskuenneth.benice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

private const val ACTION_LAUNCH_APP = "de.thomaskuenneth.benice.intent.action.ACTION_LAUNCH_APP"
private const val PACKAGE_NAME = "packageName"
private const val CLASS_NAME = "className"

class BeNiceActivity : ComponentActivity() {


    override fun onResume() {
        super.onResume()
        Intent(this, AppChooserActivity::class.java).run {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            launchApp(intent)
            finish()
        }, 500L)
    }

    private fun launchApp(intent: Intent, launchAdjacent: Boolean = true) {
        lifecycleScope.launch {
            if (ACTION_LAUNCH_APP == intent.action) {
                intent.getStringExtra(PACKAGE_NAME)?.let { packageName ->
                    intent.getStringExtra(CLASS_NAME)?.let { className ->
                        launchApp(
                            packageName = packageName,
                            className = className,
                            launchAdjacent = launchAdjacent
                        )
                    }
                }
            }
        }
    }
}

fun Context.launchApp(
    packageName: String,
    className: String,
    launchAdjacent: Boolean
) {
    Intent().run {
        component = ComponentName(
            packageName,
            className
        )
        addFlags(FLAG_ACTIVITY_NEW_TASK)
        if (launchAdjacent) {
            addFlags(
                FLAG_ACTIVITY_LAUNCH_ADJACENT or
                        FLAG_ACTIVITY_TASK_ON_HOME
            )
        }
        startActivity(this)
    }
}

fun Context.createBeNiceLaunchIntent(appInfo: AppInfo) =
    Intent(this, BeNiceActivity::class.java).also { intent ->
        intent.action = ACTION_LAUNCH_APP
        intent.addFlags(
            FLAG_ACTIVITY_NEW_TASK or
                    FLAG_ACTIVITY_CLEAR_TASK
        )
        intent.putExtra(PACKAGE_NAME, appInfo.packageName)
        intent.putExtra(CLASS_NAME, appInfo.className)
    }
