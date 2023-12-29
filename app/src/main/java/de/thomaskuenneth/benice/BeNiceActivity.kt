package de.thomaskuenneth.benice

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val ACTION_LAUNCH_APP = "de.thomaskuenneth.benice.intent.action.ACTION_LAUNCH_APP"
private const val PACKAGE_NAME = "packageName"
private const val CLASS_NAME = "className"

class BeNiceActivity : ComponentActivity() {

    private lateinit var shortcutManager: ShortcutManager

    private val installedAppsFlow = MutableStateFlow(emptyList<AppInfo>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortcutManager = getSystemService(ShortcutManager::class.java)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = defaultColorScheme()
            ) {
                BeNiceScreen(
                    installedAppsFlow = installedAppsFlow,
                    onClick = ::onClick,
                    onAddLinkClicked = ::onAddLinkClicked,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                )
            }
        }
        launchApp(intent)
        lifecycleScope.launch {
            installedAppsFlow.value = installedApps()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        launchApp(intent)
    }

    private fun installedApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return mutableListOf<AppInfo>().also { list ->
            packageManager.queryIntentActivities(intent, 0).forEach { info ->
                if (info.activityInfo.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
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
        }.sortedBy { it.label }
    }

    private fun launchApp(intent: Intent) {
        lifecycleScope.launch {
            delay(500)
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

    private fun launchApp(packageName: String, className: String) {
        Intent().run {
            component = ComponentName(
                packageName,
                className
            )
            addFlags(
                FLAG_ACTIVITY_LAUNCH_ADJACENT or
                        FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
            startActivity(this)
        }
    }

    private fun onClick(appInfo: AppInfo) {
        with(appInfo) {
            launchApp(packageName = packageName, className = className)
        }
    }

    private fun onAddLinkClicked(appInfo: AppInfo) {
        if (shortcutManager.isRequestPinShortcutSupported) {
            val shortcutInfo = ShortcutInfo.Builder(this, appInfo.className)
                .setIcon(Icon.createWithAdaptiveBitmap(appInfo.icon.toBitmap()))
                .setShortLabel(appInfo.label)
                .setIntent(Intent(this, BeNiceActivity::class.java).also { intent ->
                    intent.action = ACTION_LAUNCH_APP
                    intent.putExtra(PACKAGE_NAME, appInfo.packageName)
                    intent.putExtra(CLASS_NAME, appInfo.className)
                })
                .build()
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }
}
