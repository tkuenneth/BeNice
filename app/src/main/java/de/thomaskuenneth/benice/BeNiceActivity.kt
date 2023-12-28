package de.thomaskuenneth.benice

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

private const val ACTION_LAUNCH_APP = "de.thomaskuenneth.benice.intent.action.ACTION_LAUNCH_APP"
private const val PACKAGE_NAME = "packageName"
private const val CLASS_NAME = "className"

class BeNiceActivity : ComponentActivity() {

    private lateinit var shortcutManager: ShortcutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortcutManager = getSystemService(ShortcutManager::class.java)
        enableEdgeToEdge()
        val intent = Intent(Intent.ACTION_MAIN).also {
            it.addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val list = mutableListOf<AppInfo>()
        val activities = packageManager.queryIntentActivities(intent, 0)
        activities.forEach { info ->
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
        setContent {
            MaterialTheme(
                colorScheme = defaultColorScheme()
            ) {
                Box(Modifier.statusBarsPadding()) {
                    TwoApps(
                        appInfoList = list.sortedBy { it.label },
                        onClick = ::onClick,
                        onAddLinkClicked = ::onAddLinkClicked
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (ACTION_LAUNCH_APP == intent?.action) {
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

    private fun onClick(appInfo: AppInfo) {
        with(appInfo) {
            launchApp(packageName = packageName, className = className)
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
                        FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(this)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TwoApps(
    appInfoList: List<AppInfo>,
    onClick: (AppInfo) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit
) {
    var contextMenuAppInfo by rememberSaveable { mutableStateOf<AppInfo?>(null) }
    val haptics = LocalHapticFeedback.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        appInfoList.forEach { appInfo ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onClick(appInfo) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                contextMenuAppInfo = appInfo
                            },
                            onLongClickLabel = stringResource(id = R.string.open_context_menu)
                        )
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconImage(drawable = appInfo.icon)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = appInfo.label, style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
    if (contextMenuAppInfo != null) {
        ModalBottomSheet(
            onDismissRequest = { contextMenuAppInfo = null },
            windowInsets = WindowInsets(0),
        ) {
            MenuItem(
                onClick = {
                    contextMenuAppInfo?.let {
                        onClick(it)
                    }
                    contextMenuAppInfo = null
                },
                imageRes = R.drawable.baseline_launch_24,
                textRes = R.string.launch
            )
            MenuItem(
                onClick = {
                    contextMenuAppInfo?.let {
                        onAddLinkClicked(it)
                    }
                    contextMenuAppInfo = null
                },
                imageRes = R.drawable.baseline_add_link_24,
                textRes = R.string.add_link
            )
        }
    }
}

@Composable
fun AppIconImage(drawable: Drawable) {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null
        )
    }
}

@Composable
fun MenuItem(
    onClick: () -> Unit,
    @DrawableRes imageRes: Int,
    @StringRes textRes: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(all = 16.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = textRes),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
